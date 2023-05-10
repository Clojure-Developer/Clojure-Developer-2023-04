(ns otus-02.homework.pangram
  (:require [otus-02.homework.palindrome :refer [normalize-str, alphabet]]))



(defn is-pangram [test-string]
  (= alphabet
     (set (normalize-str test-string))))

