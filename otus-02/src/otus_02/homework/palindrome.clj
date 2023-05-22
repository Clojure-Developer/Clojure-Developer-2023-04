(ns otus-02.homework.palindrome
  (:require [clojure.string :as str]))

(defn is-palindrome [test-string]
  (let [prepared-str (str/lower-case (str/replace test-string #"\W" ""))]
    (= prepared-str (str/reverse prepared-str))))
