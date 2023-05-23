(ns otus-02.homework.palindrome
  (:require [clojure.string :as string]))


(defn is-palindrome [test-string]
  (let [clean-string (-> test-string
                         (string/lower-case)
                         (string/replace #"[^a-z]" ""))]
    (= clean-string
       (string/reverse clean-string))))
