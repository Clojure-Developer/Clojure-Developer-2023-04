(ns otus-03.core
  (:require [otus-03.functions :as f])
  (:gen-class))

;;(f/private)

(defn -main [& args]
  (println
   (f/analyze-sound
    (first args))))
