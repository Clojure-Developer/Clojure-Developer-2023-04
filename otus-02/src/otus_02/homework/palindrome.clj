(ns otus-02.homework.palindrome
  (:require [clojure.string :as s]))

(def alphabet (set "abcdefghijklmnopqrstuvwxyz"))

(defn normalize-str [s-not-normal]
  (filter some? 
          (map alphabet 
               (s/lower-case s-not-normal))))

(defn is-palindrome [test-string]
  (= (normalize-str test-string) 
     (normalize-str (s/reverse test-string))))

