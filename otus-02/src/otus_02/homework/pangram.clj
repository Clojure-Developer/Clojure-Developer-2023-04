(ns otus-02.homework.pangram
  (:require [clojure.string :as str]))

(def alphabet-set (->> (range (int \a) (inc (int \z)))
                       (map char)
                       set))

(defn string->normalized-set [test-string]
  (-> test-string
      str/lower-case
      str/trim
      (str/replace #"[^a-z0-9]" "")
      set))

(defn is-pangram [test-string]
  (= alphabet-set (string->normalized-set test-string)))
