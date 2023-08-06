(ns url-shortener.web
  (:require
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.util.response :refer [response redirect not-found created]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.json :refer [wrap-json-response wrap-json-params]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [compojure.core :refer :all]
   [url-shortener.core :as shortener]))


(defroutes router
  (POST "/shorten" [url]
    (let [short-url (shortener/shorten-url url)]
      (created short-url {:url short-url})))

  (GET "/urls" []
    (response {:urls (shortener/get-all-urls)}))

  (GET "/:short-url" [short-url]
    (if-some [url (shortener/find-long-url short-url)]
      (redirect url)
      (not-found {:error "requested URL not fount"}))))


(def app
  (-> #'router
      wrap-params
      wrap-keyword-params
      wrap-json-params
      wrap-json-response))


(defn run-server []
  (let [server (run-jetty
                (wrap-reload app)
                {:port  8000
                 :join? false})]
    (println "server started on port 8000")
    server))
