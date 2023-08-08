(ns url-shortener.main
  (:gen-class)
  (:require
   [url-shortener.core :as shortener]
   [url-shortener.web :as web])
  (:import
   [org.eclipse.jetty.server Server]))

(defonce app-server
  (atom nil))

(defn -main [& args]
  (shortener/init-storage)
  (let [server (web/run-server)]
    (reset! app-server server)))

(defn start []
  (-main))

(defn stop []
  (let [^Server server @app-server]
    (when server
      (.stop server)
      (reset! app-server nil))))

(comment
  (deref app-server)
  (start)
  (stop)

  (require '[clojure.java.shell :refer [sh]])

  (sh "curl" "-X" "POST"
      "-H" "Content-Type: application/json"
      "http://localhost:8000/shorten"
      "-d" "{\"url\": \"https://clojure.org/\"}")
  
  (sh "curl" "-X" "POST"
      "-H" "Content-Type: application/json"
      "http://localhost:8000/shorten"
      "-d" "{\"url\": \"https://bit.ly/\"}")

  (sh "curl" "http://localhost:8000/d")

  (sh "curl" "http://localhost:8000/urls"))
