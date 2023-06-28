(ns spec-sampler.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer [html]]
            [hiccup.util :refer [url]]
            [spec-sampler.parse :refer [spec-valid?]]
            [spec-sampler.generate :refer [genrate-sample]])
  (:gen-class))

(defn page [title & body]
  [:html
   [:head
    [:title title]]
   [:body
    body]])


(defn page-spec [spec]
  (if (nil? spec)
    (html
     (page
      "Лучший генератор по спеке"
       [:form {:method "POST"}
        [:p "Введите спеку:"]
        [:p [:textarea {:name "spec"}]]
        [:input {:type "submit"}]]))
    {:headers {"Content-Type" "application/json"}
     :body (genrate-sample spec)}))


(defroutes router

  (GET "/" [spec]
    (page-spec spec))

  (POST "/" [spec]
    (if (spec-valid? spec)
      (redirect (str (url "/" {:spec spec})))
      (redirect (str (url "/err")))))

  (GET "/err" []
    {:status 400
     :body "Spec is not valid"})

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
