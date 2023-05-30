(ns otus-02.homework.pangram
  (:require [clojure.string :as string]))


(def alphabet
  (->> (range (int \a) (inc (int \z)))
       (map char)
       (set)))


(defn is-pangram [test-string]
  (let [clean-string (-> test-string
                         (string/lower-case)
                         (string/replace #"[^a-z]" ""))]
    (= alphabet (set clean-string))))
