(ns mini-redis.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.core.async :as a])
  (:import
   [java.io BufferedReader Writer]
   [java.net ServerSocket])
  (:gen-class))


;; используем атом в качестве in-memory хранилища
(def database
  (atom {}))


(def keys-to-expire
  (atom []))


(defn expire-key [key]
  (fn []
    (swap! database assoc key nil)
    (swap! keys-to-expire
           (fn [expire-keys]
             (remove #(= (second %) key) expire-keys)))))


(defn set-key-to-expire [key timestamp]
  (swap! keys-to-expire
         (fn [expire-keys]
           (->> (conj expire-keys [timestamp key])
                (sort-by first)
                (vec)))))


(def keys-to-clean-up
  (a/chan))


(defn start-cleanup-worker []
  (a/go-loop []
    ;; принимаем сообщения для удаления ключей из базы
    (let [{:keys [key delay timestamp]} (a/<! keys-to-clean-up)]
      ;; для каждого ключа создаём свой go блок
      (a/go
        (set-key-to-expire key timestamp)
        ;; паркуем блок до нужного момента
        (a/<! (a/timeout delay))
        ;; удаляем ключ из базы
        (expire-key key))
      (recur))))


;; канал для рассылки публикаций
(def publications-channel
  (a/chan 1))

;; специальный объект для подписки на сообщения
(def publications
  (a/pub publications-channel :channel))



;; обрабатываем команды от клиентов
(defmulti handle-command
          (fn [_ctx [command & _]]
            (keyword (str/lower-case command))))


(defmethod handle-command :ping
  [_ctx _]
  "PONG")


(defmethod handle-command :echo
  [_ctx [_ [arg-len arg]]]
  arg)


(defmethod handle-command :set
  [_ctx [_ [key-len key val-len val opt-len opt optarg-len optarg]]]
  ;; сохраняем значение
  (swap! database assoc key val)

  (when (and opt (= (.toUpperCase opt) "PX"))
    (let [delay        (Integer/parseInt optarg)
          current-time (System/currentTimeMillis)
          timestamp    (+ current-time delay)]
      ;; отправляем сообщение в канал для удаления ключей из базы
      (a/put! keys-to-clean-up
              {:key       key
               :delay     delay
               :timestamp timestamp})))

  ;; ответ клиенту
  "OK")


(defmethod handle-command :get
  [_ctx [_ [key-len key]]]
  (let [entry (find @database key)]
    (if (and (some? entry) (some? (val entry)))
      (val entry)
      "(nil)")))


(defmethod handle-command :subscribe
  [{:keys [pub-listener client-subscriptions]}
   [_ [_channel-name-len channel-name]]]
  (let [channel-listener (a/chan 1)]
    (a/sub publications channel-name channel-listener) ;; подписываем канал на публикации по топику
    (a/admix pub-listener channel-listener) ;; мерджим сообщения из канала для топика в общий канал сообщений клиента
    (swap! client-subscriptions assoc channel-name channel-listener) ;; сохраняем канал для отписки от топика
    ["subscribe" channel-name (count @client-subscriptions)]))


(defmethod handle-command :unsubscribe
  [{:keys [pub-listener client-subscriptions]}
   [_ [_channel-name-len channel-name]]]
  (let [channel-listener (get @client-subscriptions channel-name)]
    (a/unmix pub-listener channel-listener) ;; убираем сообщения канала для топика из общего канал сообщений клиента
    (a/close! channel-listener) ;; закрываем канал
    (swap! client-subscriptions dissoc channel-name) ;; убираем канал из атома
    ["unsubscribe" channel-name (count @client-subscriptions)]))


(defmethod handle-command :publish
  [_ctx [_ [_channel-name-len channel-name _message-len message]]]
  ;; отправляем все публикации в глобальный канал для рассылки всем подписавшимся клиентам
  (a/put! publications-channel
          {:channel channel-name ;; по этому ключу выбираются каналы клиентов куда будет доставлено сообщение
           :message ["*3" "$7" "MESSAGE" channel-name message]})
  1)


(defmethod handle-command :message
  [_ctx [_ [channel-name message]]]
  ["message" channel-name message])


;; needed for redis-cli
(defmethod handle-command :command
  [_ctx _]
  "O hai")



;; обрабатываем сообщения от клиента
(defn error? [data]
  (-> (class data)
      (supers)
      (contains? Throwable)))


(defn reply [data]
  ;; форматируем ответ клиенту согласно протоколу Redis
  (let [data-type (cond
                    (string? data) "+"
                    (integer? data) ":"
                    (error? data) "-"
                    (vector? data) (str "*" (count data) "\r\n"))
        data      (if (vector? data)
                    (->> data
                         (mapcat #(cond (integer? %) [(str ":" %)]
                                        (string? %) [(str "$" (count %)) %]))
                         (str/join "\r\n"))
                    data)]
    (str/join [data-type data "\r\n"])))


(defn handle-message
  "Pass parsed message to the dispatch function and format result for client"
  [ctx message]
  (let [[number-of-arguments command-string-len command & args] message]
    (println "handling command" command)
    (-> (handle-command ctx [command args])
        (reply))))


(defn read-message [^BufferedReader socket-reader]
  ;; .readLine блокирующий вызов поэтому выносим в отдельный поток
  (a/thread
   (loop [line (.readLine socket-reader)
          res  []]
     (cond
       ;; сокет закрылся
       (nil? line) res
       ;; клиент ничего не ввёл, но соединение еще открыто
       (not (.ready socket-reader)) (conj res line)
       ;; читаем следующую строку
       :otherwise (recur (.readLine socket-reader)
                         (conj res line))))))


(defn read-messages
  "Read all lines of textual data from the given socket"
  [^ServerSocket socket]
  (let [messages-channel (a/chan)
        socket-reader    (io/reader socket)]
    (a/go-loop []
      ;; получаем сообщение от клиента
      (let [message (a/<! (read-message socket-reader))]
        (if (empty? message)
          ;; клиент отключился, подчищаем ресурсы
          (do (a/close! messages-channel)
              (.close socket-reader))

          (do (println "got message" message)
              (a/>! messages-channel message)
              (recur)))))
    messages-channel))


(defn send-message
  "Send the given string message out over the given socket"
  [^Writer socket-writer ^String msg]
  (.write socket-writer msg)
  (.flush socket-writer))


(defn handle-client
  "Create a separate thread for each client to execute commands"
  [socket handler]
  ;; запускаем отдельный go блок для каждого клиента
  (let [messages      (read-messages socket) ;; канал в который попадают сообщения из сокета самого клиента
        publications  (a/chan 1 (map :message)) ;; канал в который попадают сообщения из сокетов других клиентов
        ctx           {:pub-listener         (a/mix publications) ;; mix похож на a/merge, но позволяет динамически добавлять и удалять каналы
                       :client-subscriptions (atom {})}
        all-messages  (a/merge [publications messages]) ;; объединяем все сообщения в один канал
        responses     (a/pipe all-messages (a/chan 1 (map (partial handler ctx)))) ;; создаём пайплайн для трансформации сообщений в ответы клиентам
        socket-writer (io/writer socket)]
    (a/go-loop []
      (let [response (a/<! responses)]
        (println "got response" response)
        (if (some? response)
          ;; отправляем ответ
          (do (send-message socket-writer response)
              (recur))
          ;; если приходит nil сокет закрылся со стороны клиента
          (.close socket-writer))))))


(defn accept-connection [^ServerSocket server-socket]
  ;; метод .accept блокирующий, поэтому выносим в отдельный поток
  (a/thread
   (.accept server-socket)))


(defn run-server
  "Run socket server on the specified port"
  [port handler]
  ;; создаём сокет сервер
  (let [server-sock (ServerSocket. port)]
    (.setReuseAddress server-sock true)

    ;; обрабатываем подключения в пользователей в go блоке
    (a/go-loop []
      ;; тут go блок не блокируется, а паркуется
      (let [socket (a/<! (accept-connection server-sock))]
        (println "got connection")
        (#'handle-client socket handler)
        (recur)))

    ;; возвращаем объект сервера
    server-sock))


;; graceful shutdown
(defn shutdown-hook [server]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. ^Runnable
                             (fn []
                               (.close server)
                               (shutdown-agents)))))


;; точка входа
(defn -main
  [& args]
  (let [server (run-server 6379 handle-message)]
    (start-cleanup-worker)
    (shutdown-hook server)
    server))



(comment
 (def state
   (-main))

 @database
 @keys-to-expire

 (.close server)

 nil)


