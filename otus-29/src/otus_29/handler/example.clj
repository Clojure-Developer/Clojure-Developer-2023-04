(ns otus-29.handler.example
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [integrant.core :as ig]))

(defmethod ig/init-key :otus-29.handler/example [_ options]
  (context "/example" []
    (GET "/" []
      (io/resource "otus_29/handler/example/example.html"))))
