(ns otus-14.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.handler.dump :refer [handle-dump]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.coercions :refer [as-int]]
            [hiccup.core :refer [html]]
            [hiccup.util :refer [url]]
            [cheshire.core :refer [generate-string]])
  (:gen-class))

(defn page [title & body]
  [:html
   [:head
    [:title title]]
   [:body
    body]])

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (html
    (page
     "Index"
     [:h1 "Hello world!"]))})

(defn greet-page [name]
  (html
   (page
    "greet"
    (if (nil? name)
      [:form {:method "POST"}
       [:label "Name"]
       [:input {:name "name"}]
       [:button {:type "submit"} "Greet"]]
      [:h1 (str "Hello, " name "!")]))))

(defroutes router
  (GET "/" []
    (html
     (page
      "Index"
      [:h1 "Hello world!"])))

  (GET "/greet" [name] (greet-page name))

  (POST "/greet" [name]
    (redirect (str (url "/greet" {:name name}))))

  (GET "/dump" req
    (handle-dump req))

  (GET "/json-dump" []
    {:headers {"Content-Type" "application/json"}
     :body
     (generate-string
      {:users
       [{:name "Bob"
         :admin false
         :pets [{:name "Tom"
                 :age 4}]}]})})

  (GET "/add" [x :<< as-int
               y :<< as-int]
    (html [:h1 (+ x y)]))

  (route/not-found
   (html
    (page
     "Page not found"
     [:h1 "Oops!"]))))

(def app
  (-> #'router
      wrap-keyword-params
      wrap-params))

(comment
  (run-jetty #'app {:join? false
                    :port 8000}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run-jetty
   (wrap-reload app)
   {:port 8000}))
