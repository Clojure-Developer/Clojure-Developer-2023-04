(ns otus-02.homework.palindrome
  (:require [clojure.string :as str]))

(defn normalize [test-string]
  (-> test-string
      str/lower-case
      str/trim
      (str/replace #"[^a-z0-9]" "")))

(defn is-palindrome [test-string]
  (let [string (normalize test-string)]
    (= string (str/reverse string))))
