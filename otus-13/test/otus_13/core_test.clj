(ns otus-13.core-test
  (:require
   [cheshire.core :as cheshire]
   [clojure.test :refer :all]
   [clj-http.client :as http]
   [clj-http.fake :refer [with-fake-routes]]))


(defn get-slideshow []
  (-> (http/get "http://localhost:80/json"
                {:as :json})
      :body
      :slideshow))


(deftest fake-http-example-test

  (with-fake-routes
   {"http://localhost:80/json"
    (fn [request]
      {:status 200
       :body   (cheshire/generate-string
                {:slideshow {:author "Nikola Tesla"
                             :title  "Electric power transmission"}})})}

   (is (= "Electric power transmission"
          (:title (get-slideshow)))))




  #_(with-fake-routes
     {"http://localhost:80/json"
      ;;#"http:.*/json"
      ;;{:address "http://localhost:80/json" :query-params {:search "author"}}
      (fn [request]
        {:status 200
         :body   (cheshire/generate-string
                  {:slideshow {:author "Nikola Tesla"
                               :title  "Electric power transmission"}})})})

  #_(with-fake-routes
     {"http://localhost:80/json"
      {:get  (fn [request] {:status 200 :body "GET"})
       :post (fn [request] {:status 200 :body "POST"})}}))
