(ns otus-02.homework.palindrome
  (:require [clojure.string :as string]))


(defn is-palindrome [test-string]
  (let [alphanum-only-string (string/lower-case (string/replace test-string #"\W" ""))]
    (= alphanum-only-string (string/reverse alphanum-only-string))))
