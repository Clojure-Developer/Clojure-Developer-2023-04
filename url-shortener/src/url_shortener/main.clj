(ns url-shortener.main
  (:gen-class)
  (:require
   [url-shortener.core :as shortener]
   [url-shortener.web :as web])
  (:import
   [org.eclipse.jetty.server Server]))


(def app-server
  (atom nil))


(defn -main [& args]
  (shortener/init-storage)
  (let [server (web/run-server)]
    (reset! app-server server)))


(defn stop []
  (let [^Server server @app-server]
    (.stop server)))




(comment
 (-main)

 (require '[clojure.java.shell :refer [sh]])

 (sh "curl" "-X" "POST"
     "-H" "Content-Type: application/json"
     "http://localhost:8000/shorten"
     "-d" "{\"url\": \"https://clojure.org/\"}")

 (sh "curl" "http://localhost:8000/b")

 (sh "curl" "http://localhost:8000/urls")

 (stop))
