(ns otus-30.core
  (:gen-class)
  (:require
   [com.brunobonacci.mulog :as mulog]
   [nrepl.server :as nrepl]))


(defn start-repl-server [port]
  (nrepl/start-server
   :port port
   :bind "0.0.0.0"))


(defn -main [& args]
  (start-repl-server 9999)

  (println "Hello world")

  (let [stop-mulog (mulog/start-publisher!
                    {:type    :console
                     :pretty? true})]

    (mulog/log ::app-started
               :timestamp (System/currentTimeMillis))

    (println "Hello world")

    (stop-mulog)))
