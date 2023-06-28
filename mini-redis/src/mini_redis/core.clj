(ns mini-redis.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str])
  (:import
   [java.io BufferedReader Writer]
   [java.net ServerSocket])
  (:gen-class))


;; используем атом в качестве in-memory хранилища
(def database
  (atom {}))



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
    (let [timeout (Integer/parseInt optarg)]
      (future
       (Thread/sleep timeout)
       (swap! database assoc key nil))))
  ;; ответ клиенту
  "OK")


(defmethod handle-command :get
  [[_ [key-len key]]]
  (if-some [entry (find @database key)]
    (val entry)
    "(nil)"))


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
    (shutdown-hook server)
    server))



(comment
 (def server
   (-main))

 @database

 (.close server)
 nil)
