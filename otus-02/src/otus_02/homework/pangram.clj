(ns otus-02.homework.pangram
  (:require [clojure.string :as s]))

(def alphabet "abcdefghijklmnopqrstuvwxyz")

(def pattern (re-pattern (str "[^" alphabet "]")))

(defn is-pangram
  "Проверяет, является ли строка панграммой для заданного выше алфавита."
  [test-string]
  (-> test-string
      s/lower-case
      (s/replace pattern "")
      set
      count
      (= (count alphabet))))
