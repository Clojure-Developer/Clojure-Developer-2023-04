(ns otus-05.core
  (:gen-class)
  (:require [example.core :refer [greet]]))

(defn -main [& args]
  (greet "fellow clojurista!"))
