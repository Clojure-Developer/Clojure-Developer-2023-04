(ns otus-16.core)



;; =============================================================================
;; Потоки в Java
;; =============================================================================

;; java.lang.Thread

;; создаём поток с помощью конструктора
(def thread-1
  (Thread. ^Runnable
           (fn []   ;; 0 arguments
             ;; this will run in a new Thread
             (println 1 2 3))))

;; запускаем поток
(.start thread-1)


;; создаём поток с помощью объекта реализующего интерфейс Runnable
(def thread-2
  (Thread. (reify Runnable
             (run [_]
               (Thread/sleep 4000)
               (println 4 5 6)))))

(.start thread-2)



;; поток нельзя запустить повторно
;; состояния потока
#_(.start thread-2)


;; проверяем запущен ли поток
(.isAlive thread-2)



;; блокируем основной поток с помощью .join
(def thread-3
  (Thread.
   ^Runnable
   (fn []
     (Thread/sleep 100)
     (println "print from third"))
   "third thread")) ;; второй аргумент имя потока


;; сначала выполнится основной поток
(do (.start thread-3)
    (println "print from main"))

;; сначала выполнится новый поток
(do (doto thread-3
      (.start)
      (.join))
    (println "print from main"))



;; получаем все запущенные потоки
(keys (Thread/getAllStackTraces))


(-> (Thread.
     ^Runnable
     (fn []
       (Thread/sleep 5000))
     "I'm alive")
    (.start))


;; находим поток по имени
(->> (keys (Thread/getAllStackTraces))
     (filter #(= (.getName ^Thread %) "I'm alive"))
     ^Thread first
     (.isAlive))




;; прерываем выполнение потока
(def interrupted
  (Thread.
   ^Runnable
   (fn []
     (Thread/sleep 5000)
     (println "print from interrupted"))
   "I'm alive"))

(do (.start interrupted)
    (Thread/sleep 2500)
    (.interrupt interrupted)) ;; посылает сигнал потоку о прерывании



;; перехватываем ошибку внутри потока
(doto (Thread.
       ^Runnable
       (fn []
         (try
           (Thread/sleep 5000)
           (println "print from interrupted")
           (catch InterruptedException ex
             (println "interrupted, stopping")))))
  (.start)
  (.interrupt))



;; обрабатываем экстренное завершение работы приложения
(.addShutdownHook (Runtime/getRuntime)
                  ;; этот поток будет запущен при завершении работы виртуальной машины
                  ;; 1. в штатном режиме (когда все инструкции выполнились и нет запущенных user потоков)
                  ;; 2. когда был вызван System.exit явно
                  ;; 3. в случае прерывания работы процесса извне (SIGTERM, SIGKILL, etc.)
                  (Thread. ^Runnable
                           (fn []
                             (println "shutting down!"))))



;; =============================================================================
;; Future
;; =============================================================================

(future
 ;; код внутри будет выполнен в отдельном потоке
 (Thread/sleep 2000)
 (println "hello future"))


;; используется в качестве callback (выполняем блокирующее действие и возвращаем результат)
(def response
  (future
   (println "http request")
   (Thread/sleep 4000)
   35))

@response           ;; блокирует основной поток пока отдельный поток не выполнится

(when
 (realized? response
  @response))

(deref response)

(deref response 500 :not-found) ;; читаем с таймаутом




;; используется в качестве worker (отдельный поток работает параллельно основному потоку)
;; сканирование файловой системы, чтение сообщений из Kafka, отправка логов в хранилище и тд
(def worker
  (future
   (while true
     (println "reading messages from Kafka")
     (Thread/sleep 1000))))


;; останавливаем поток
(future-cancel worker)

(future-cancelled? worker)
(future-done? worker)

;; у закрытого потока нельзя прочитать значение
@worker



;; future запускает user threads
;; shutdown-agents останавливает все запущенные потоки через future
(.addShutdownHook (Runtime/getRuntime)
                  (Thread. ^Runnable
                           (fn []
                             (shutdown-agents))))




;; =============================================================================
;; Promise
;; =============================================================================

;; используются для синхронизации состояния между потоками

(def p
  (promise))

p

(realized? p)

(deliver p {:result 42}) ;; установить значение можно только один раз

(realized? p)

;; получаем значение промиса
@p

(deref p)

;; читаем с таймаутом и дефолтным значением
(deref (promise) 500 :nothing-to-see-here)




;; сигнализируем потокам об изменении состояния из главного потока
(def shutdown?
  (promise))

(-> (Thread.
     #(while (not (realized? shutdown?)) ;; проверяем что у промиса нет установленного значения
        (println "doing some work...")
        (Thread/sleep 2000)))
    (.start))


(deliver shutdown? true)


;; закрываем все потоки перед завершением работы приложения
(.addShutdownHook (Runtime/getRuntime)
                  (Thread. ^Runnable
                           (fn []
                             (deliver shutdown? true))))



;; =============================================================================
;; Delay
;; =============================================================================

;; используется для отложенного выполнения кода

(def millis
  (delay
   ;; этот код не выполняется сразу
   (do
     (println "calculating..")
     (System/currentTimeMillis))))

millis

(realized? millis)

;; вычисляем значение (выполняем код, который указали при создании)
@millis

(force millis)


(realized? millis)

;; полученное значение кэшируется
@millis



;; используется для реализации singleton паттерна
(defn get-db-connection []
  (Thread/sleep 1000)
  (println "Connection created!"))


;; плохой пример
(def connection
  (atom nil))

(defn get-or-create-connection []
  (if-let [conn @connection] ;; каждый поток "подумает" что соединение с базой еще не открыто
    conn
    (swap! connection
           (fn [current-connection]
             (or current-connection (get-db-connection))))))

;; каждый поток попытается открыть соединение
(dotimes [_ 10]
  (-> (Thread. ^Runnable
               (fn []
                 (let [conn (get-or-create-connection)]
                   "doing some work")))
      (.start)))




;; хороший пример
(def connection
  (delay (get-db-connection)))

(dotimes [_ 10]
  (-> (Thread. ^Runnable
               (fn []
                 ;; первый поток, который вызвал deref запустит подключение к базе
                 ;; остальные будут заблокированы
                 (let [conn @connection]
                   "doing some work")))
      (.start)))




;; =============================================================================
;; Agent
;; =============================================================================

;; похож на атом, но обновление происходит в отдельном потоке из thread pool
;; (один вызов в любой момент времени)

(def my-counter
  (agent 0))


@my-counter

;; изменяем состояние агента (аналог swap!)
(send my-counter inc)
(send-off my-counter inc)


;; изменение состояния агента не блокирует основной поток
;; в отличие от атома
(do
  (send my-counter
        (fn [counter]
          (Thread/sleep 5000)
          (println "agent thread")
          (inc counter)))
  (println "main thread"))


;; блокируем основной поток явно
(await my-counter)

(await-for 1000 my-counter)




;; agent принимает дополнительные аргументы
(def counter-with-opts
  (agent 0
         :meta {:name 'counter-with-opts}
         :validator (fn [n] (<= n 5))
         :error-handler (fn [agent error] (println error))
         :error-mode (or :continue :fail)))

(send counter-with-opts inc)

@counter-with-opts


;; агенты в failed state можно перезапустить
(def failing-mode-agent
  (agent 0 :validator (fn [n]
                        (Thread/sleep 1000)
                        (<= n 5))))

(send failing-mode-agent inc)

@failing-mode-agent

(agent-error failing-mode-agent)

;; после рестарта все обновления в очереди продолжат применяться
(restart-agent failing-mode-agent 0)

;; обновления в очереди будут удаленны
(restart-agent failing-mode-agent 0 :clear-actions true)



;; send для быстрых (не блокирующих) задач
(time
 (let [agents (map #(agent %) (range 100))]
   (doseq [a agents]
     (send a (fn [_] (Thread/sleep 1000))))
   (apply await agents)))



;; send-off для долгих (блокирующих) задач
(time
 (let [agents (map #(agent %) (range 100))]
   (doseq [a agents]
     (send-off a (fn [_] (Thread/sleep 1000))))
   (apply await agents)))




;; пример асинхронного логгера

(def logger
  (agent []))


(defn log [message]
  (send logger conj message))


(defn publish [logs pub-num]
  (if (empty? logs)
    logs
    (let [logs (->> logs
                    (map (fn [log]
                           (str log ":publication " pub-num)))
                    (clojure.string/join "\n"))
          logs (str logs "\n")]
      (spit "app.log" logs :append true)
      [])))


(def publisher
  (future
   (doseq [pub-num (range)]
     (println "publish")
     (Thread/sleep 1000)
     (send-off logger publish pub-num))))



(defn simulate-processes []
  (mapv (fn [thread-num]
          (future
           (while true
             (Thread/sleep ^Long (-> (rand-int 5) inc (* 100)))
             (log (str "message from thread " thread-num)))))
        (range 10)))

(defn stop [processes]
  (doseq [p processes]
    (future-cancel p)))

(def processes
  (simulate-processes))

(stop processes)
(future-cancel publisher)



;; =============================================================================
;; Locks
;; =============================================================================

;; в основном используется в операциях над Java объектами

(def numbers-array
  (make-array Integer/TYPE 8))

(seq numbers-array)

#_(defn update-at [arr idx func]
    (let [current-val (aget arr idx)]
      (aset arr idx (func current-val))))


(defn update-at [arr idx func]
  (locking arr      ;; блокируем доступ к объекту в других потоках
    (let [current-val (aget arr idx)]
      (aset arr idx (func current-val)))))


(dotimes [_ 100]
  (future
   (dotimes [i (count numbers-array)]
     (update-at numbers-array i inc))))
