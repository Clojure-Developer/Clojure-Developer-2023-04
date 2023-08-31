(ns otus-31.criterium
  (:require
   [criterium.core :as criterium]))

;; * worst
(time (reduce + (map #(/ % 100.0) (range 100))))

;; * better
(time (dotimes [_ 1e6] (reduce + (map #(/ % 100.0) (range 100)))))

;; * better
(criterium/quick-bench (reduce + (map #(/ % 100.0) (range 100))))
