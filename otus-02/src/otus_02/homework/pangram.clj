(ns otus-02.homework.pangram
  (:require [clojure.string :as str]))

(defn is-pangram [test-string]
  (let [alphabet (vec "abcdefghijklmnopqrstuvwxyz")]
    (= alphabet
       (sort (distinct
               (str/lower-case (str/replace test-string #"\W" "")))))))
