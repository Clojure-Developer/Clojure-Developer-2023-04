(ns otus-02.homework.palindrome
  (:require [clojure.string :as str]))

(defn is-palindrome [test-string]
  (let [original (str/lower-case
                   (str/replace test-string #",|!|\?|\.|\s" ""))
        reversed (str/reverse original)]
    (= original reversed)))














