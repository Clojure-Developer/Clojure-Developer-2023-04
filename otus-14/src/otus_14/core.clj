(ns otus-14.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.handler.dump :refer [handle-dump]]
            [ring.middleware.reload :refer [wrap-reload]])
  (:gen-class))

(defn handler [req]
  {:status 200
   :body "Hello world!!"})

(def app
  (wrap-reload #'handler))

(comment
  (run-jetty #'app {:join? false
                    :port 3000}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
