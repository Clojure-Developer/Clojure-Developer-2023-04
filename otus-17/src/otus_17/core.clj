(ns otus-17.core
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [java.util.concurrent Future]))


;; =============================================================================
;; p* functions
;; =============================================================================

;; значения вычисляются в разных потоках параллельно
(pmap inc (range 10))


;; последовательность остаётся ленивой
(def numbers
  (pmap (fn [n]
          (println (str n ";"))
          n)
        (range)))

;; вычислит только N+2 элементов
;; где N - количество ядер процессора
(take 10 numbers)




;; как и pmap, но принимает набор s-expressions
(pvalues
 (+ 1 2)
 3
 (- 5 2))



(defn sleeper [s thing]
  (println "calculating" thing)
  (Thread/sleep ^Long (* 1000 s))
  thing)


(defn pvs []
  (pvalues
   (sleeper 2 :1st)
   (sleeper 3 :2nd)
   (keyword "3rd")))


(-> (pvs) first time)

;; время доступа к элементу определяется самой долгой операцией
(-> (pvs) last time)





;; как pmap, но принимает разные функции
(-> (pcalls
     #(sleeper 2 :first)
     #(sleeper 3 :second)
     #(keyword "3rd"))
    doall
    time)







(defn pmap-file [processing-fn input-file]
  (with-open [rdr (io/reader input-file)]
    (let [lines (line-seq rdr)]
      ;; обрабатываем каждую строку из файла в отдельном потоке
      (->> (pmap processing-fn lines)
           ;; агрегируем в основном потоке
           (apply merge-with +)))))


(defn word-count
  "Trivial example"
  [line]
  (-> line
      (string/replace #"[^\w\s]" "")
      (string/split #"\s")
      (frequencies)))


(pmap-file word-count "words.txt")




;; =============================================================================
;; reducers
;; =============================================================================

(require '[clojure.core.reducers :as r])


;; r/map, r/filter, r/drop, r/take, r/cat
(r/reduce + 0 (range 20))




(def numbers
  (vec (range (* 1000 1000))))


(r/fold + numbers)

(r/fold + (r/filter even? numbers))




;; возвращают reducible вместо ленивой последовательности
(type (r/filter even? (r/map inc (range 100000))))

(r/filter even? (r/map inc (range 100000)))

;; не создают промежуточных коллекций
(into [] (r/filter even? (r/map inc (range 100000))))

(r/foldcat (r/filter even? (r/map inc (range 100000))))







(defn count-words
  ([] {})
  ([freqs word]
   (assoc freqs word (inc (get freqs word 0)))))


(defn merge-counts
  ([] {})
  ([& m]
   (apply merge-with + m)))


(defn word-frequency [file]
  (let [words-seq (-> (slurp file)
                      (string/replace #"[^\w\s]" "")
                      (string/split #"\s+"))]
    (r/fold merge-counts count-words words-seq)))


(word-frequency "words.txt")



;; аналитические вычисления с помощью редьюсеров
;; https://clojuredatascience.com/posts/2015-09-12-parallel-folds-reducers-tesser-linear-regression.html






;; =============================================================================
;; Thread pool
;; =============================================================================


(import '[java.util.concurrent
          Executor Executors ExecutorService
          ScheduledExecutorService TimeUnit])



(defn demo-task []
  (println "demo task running")
  (* 2 5))

(demo-task)

(.run demo-task)

(.call demo-task)



(def thread-executor
  (reify Executor
    (execute [_ task]
      (let [^Runnable f #(try
                           (task)
                           ;; return value is ignored by Thread
                           (catch Throwable e
                             ;; not much we can do here
                             (.printStackTrace e *out*)))]
        (doto (Thread. f)
          (.start))))))


(.execute thread-executor demo-task)




;; thread pool фиксированного размера
(def ^ExecutorService pool
  (Executors/newFixedThreadPool 4))

;; выполнить одну задачу
(def task
  (.submit pool ^Callable (fn []
                            (Thread/sleep 4000)
                            "hello")))

;; возвращается объект Future
@task
(future-cancel task)




(defn sleep-print-and-double [x]
  (Thread/sleep 5000)
  (println x "done!")
  (* x 2))


;; отправляем сразу 10 задач, но выполняются они по 4 за раз
(let [tasks   (for [i (range 10)]
                #(sleep-print-and-double i))
      futures (.invokeAll pool tasks)]
  ;; дожидаемся окончания работы всех задач
  (for [ftr futures]
    @ftr))



;; можем использовать совместно с транзакциями
(defn test-stm [nitems nthreads niters]
  (let [refs  (map ref (repeat nitems 0))
        pool  (Executors/newFixedThreadPool nthreads)
        tasks (map (fn [t]
                     (fn []
                       (dotimes [n niters]
                         (dosync
                          (doseq [r refs]
                            (alter r + 1 t))))))
                   (range nthreads))]

    (doseq [^Future future (.invokeAll pool tasks)]
      @future)

    ;; пулы надо закрывать явно
    (.shutdown pool)
    (map deref refs)))


(test-stm 10 10 10000)




(def cached-pool
  (Executors/newCachedThreadPool))


(.submit cached-pool #(println "thread will be dropped after 60 seconds"))




(def ^ScheduledExecutorService scheduled-pool
  (Executors/newScheduledThreadPool 1))


(def scheduled-future
  (.scheduleAtFixedRate scheduled-pool
                        #(println "doing work") 0 5 TimeUnit/SECONDS))

(future-cancel scheduled-future)



(.schedule scheduled-pool
           ^Runnable (fn []
                       (println "doing work"))
           5 TimeUnit/SECONDS)


(.shutdown scheduled-pool)



;; =============================================================================
;; Virtual threads
;; =============================================================================

;; необходимо включить JVM флаг --enable-preview
(Thread/startVirtualThread #(println "Hello world!"))


(defn thread-factory [name]
  (-> (Thread/ofVirtual)
      (.name name 0)
      (.factory)))


(set-agent-send-executor!
 (Executors/newThreadPerTaskExecutor
  (thread-factory "clojure-agent-send-pool-")))

(set-agent-send-off-executor!
 (Executors/newThreadPerTaskExecutor
  (thread-factory "clojure-agent-send-off-pool-")))


(def a-counter
  (agent 0))

(send a-counter inc)

(await a-counter)

@a-counter



(def unbounded-executor
  (Executors/newThreadPerTaskExecutor
   (thread-factory "unbounded-pool-")))

(send-via unbounded-executor a-counter dec)

(await a-counter)

@a-counter


;; https://github.com/clj-commons/claypoole
