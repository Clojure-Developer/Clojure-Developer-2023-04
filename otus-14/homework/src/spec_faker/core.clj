(ns spec-faker.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer :all]
            [hiccup.core :refer [html]])
  (:gen-class))

(defn -main
  [& args]
  (run-jetty
   (routes
    (GET "/" []
      (html [:h1 "Hello World!"])))
   {:port 8000}))
