(ns mini-redis.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str])
  (:import
   [java.io BufferedReader Writer]
   [java.net ServerSocket]
   [java.util.concurrent Executors ScheduledExecutorService TimeUnit])
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
           (-> (conj expire-keys [timestamp key])
               (sort-by first)
               (vec)))))


(comment
 @keys-to-expire
 (set-key-to-expire "name" 123123123)
 (expire-key "name"))


(def ^ScheduledExecutorService cleanup-pool
  (Executors/newScheduledThreadPool 10))


(defn schedule-cleanup-task []
  (let [current-time       (System/currentTimeMillis)
        five-seconds-later (+ current-time 5000)
        keys-to-schedule   (->> @keys-to-expire
                                (take-while #(< (first %) five-seconds-later)))]
    (doseq [[timestamp key] keys-to-schedule]
      (.schedule cleanup-pool
                 ^Runnable (expire-key key)
                 ^Long (- timestamp current-time)
                 TimeUnit/MILLISECONDS))))


(defn start-cleanup-worker []
  (.scheduleAtFixedRate cleanup-pool schedule-cleanup-task 0 5 TimeUnit/SECONDS))



;; обрабатываем команды от клиентов
(defmulti handle-command
  (fn [[command & _]]
    (keyword (str/lower-case command))))


(defmethod handle-command :ping
  [_]
  "PONG")


(defmethod handle-command :echo
  [[_ [arg-len arg]]]
  arg)


(defmethod handle-command :set
  [[_ [key-len key val-len val opt-len opt optarg-len optarg]]]
  ;; сохраняем значение
  (swap! database assoc key val)

  (when (and opt (= (.toUpperCase opt) "PX"))
    (let [delay        (Integer/parseInt optarg)
          current-time (System/currentTimeMillis)
          timestamp    (+ current-time delay)]
      (if (< timestamp (+ current-time 5000))
        (.schedule cleanup-pool ^Runnable (expire-key key) delay TimeUnit/MILLISECONDS)
        (set-key-to-expire key timestamp))))

  ;; ответ клиенту
  "OK")


(defmethod handle-command :get
  [[_ [key-len key]]]
  (let [entry (find @database key)]
    (if (some? (val entry))
      (val entry)
      "(nil)")))


;; needed for redis-cli
(defmethod handle-command :command
  [_]
  "O hai")



;; обрабатываем сообщения от клиента
(defn error? [data]
  (-> (class data)
      (supers)
      (contains? Throwable)))


(defn reply [data]
  (let [data-type (cond
                    (string? data) "+"
                    (integer? data) ":"
                    (error? data) "-")]
    (str/join [data-type data "\r\n"])))


(defn handle-message
  "Pass parsed message to the dispatch function and format result for client"
  [message]
  (let [[number-of-arguments command-string-len command & args] message]
    (-> (handle-command [command args])
        (reply))))



(comment
 (handle-command ["ECHO" [5 "HELLO"]])
 (handle-message [nil nil "ECHO" 5 "HELLO"]))



(defn read-message
  "Read all lines of textual data from the given socket"
  [^BufferedReader socket-reader]
  (loop [line (.readLine socket-reader)
         res  []]
    (cond
      ;; сокет закрылся
      (nil? line) res
      ;; клиент ничего не ввёл, но соединение еще открыто
      (not (.ready socket-reader)) (conj res line)
      ;; читаем следующую строку
      :otherwise (recur (.readLine socket-reader)
                        (conj res line)))))



(comment
 (read-message (io/reader (char-array "*2\r\n$4\r\necho\r\n$5\r\nhello")))
 (read-message (io/reader (char-array "*5\r\n$3\r\nset\r\n$4\r\nname\r\n$6\r\nSergey\r\n$2\r\nRX\r\n$5\r\n10000"))))



(defn send-message
  "Send the given string message out over the given socket"
  [^Writer socket-writer ^String msg]
  (.write socket-writer msg)
  (.flush socket-writer))



(defn handle-client
  "Create a separate thread for each client to execute commands"
  [socket handler]
  ;; запускаем отдельный поток для каждого клиента
  (future
   (with-open [reader (io/reader socket)
               writer (io/writer socket)]
     ;; обрабатываем команды от клиента
     (doseq [msg-in (repeatedly #(read-message reader))
             :while (not (empty? msg-in))
             :let [msg-out (handler msg-in)]]
       (println "msg-in" msg-in)
       ;; отправляем ответ
       (send-message writer msg-out)))))



(defn run-server
  "Run socket server on the specified port"
  [port handler]
  ;; создаём сокет сервер
  (let [server-sock (ServerSocket. port)]
    (.setReuseAddress server-sock true)

    ;; обрабатываем подключения в отдельном потоке
    (future
     (while true
       (let [socket (.accept server-sock)]
         (handle-client socket handler))))

    ;; возвращаем объект сервера
    server-sock))



;; graceful shutdown
(defn shutdown-hook [server worker]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. ^Runnable
                             (fn []
                               (.close server)
                               (future-cancel worker)
                               (.shutdown cleanup-pool)
                               (shutdown-agents)))))


;; точка входа
(defn -main
  [& args]
  (let [server (run-server 6379 handle-message)
        worker (start-cleanup-worker)]
    (shutdown-hook server worker)
    server))



(comment
 (def server
   (-main))

 @database
 @keys-to-expire

 (.close server)
 nil)
