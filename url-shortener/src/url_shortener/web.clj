(ns url-shortener.web
  (:require
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.util.response :refer [response resource-response redirect not-found created]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [compojure.core :refer [GET POST defroutes]]
   [url-shortener.core :as shortener]
   [url-shortener.boundary.short-url :as url-store]
   [integrant.core :as ig]))


(defmethod ig/init-key :url-shortener.web/handler
  [_ {:keys [db host]}]
  (defroutes router
    (GET "/" []
      (resource-response "index.html" {:root "public"}))

    (POST "/shorten" [url]
      (let [key (shortener/url->key url)
            short-url (str host key)]
        (url-store/save db key url)
        (created short-url {:url short-url})))

    (GET "/urls" []
      (response {:urls (url-store/list-all db)}))

    (GET "/:short-url" [short-url]
      (if-some [url (url-store/find-by-id db short-url)]
        (redirect url)
        (not-found {:error "requested URL not fount"}))))

  (-> #'router
      (wrap-resource "public")
      wrap-params
      wrap-keyword-params
      wrap-json-params
      wrap-json-response))


(defmethod ig/init-key :url-shortener.web/server
  [_ {:keys [port env handler]}]
  (println "Server started on port:" port)
  (let [server (run-jetty handler {:port  port
                                   :join? (if (= env :production)
                                            true
                                            false)})]
    server))

(defmethod ig/halt-key! :url-shortener.web/server
  [_ server]
  (when server
    (println "Server stopped!")
    (.stop server)))


(comment
  (require '[clojure.java.shell :refer [sh]])

  (sh "curl" "-X" "POST"
      "-H" "Content-Type: application/json"
      "http://localhost:8000/shorten"
      "-d" "{\"url\": \"https://clojure.org/\"}")

  ;; => {\"url\":\"http://localhost:8000/cWyhdn\"}

  (sh "curl" "-X" "POST"
      "-H" "Content-Type: application/json"
      "http://localhost:8000/shorten"
      "-d" "{\"url\": \"https://bit.ly/\"}")

  ;; => {\"url\":\"http://localhost:8000/d4w07h\"}

  (sh "curl" "http://localhost:8000/cWyhdn")

  (sh "curl" "http://localhost:8000/urls"))
