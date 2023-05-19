(ns otus-02.homework.pangram
  (:require [clojure.string :as string]))

(defn generate-abc []
  (map char (range (int \a) (int \z))))

(defn trim-nonalph-and-lcase [sentence]
  (string/lower-case (string/replace sentence #"[^a-zA-Z]" "")))

(defn is-pangram [test-string]
  (every? (set (trim-nonalph-and-lcase test-string))
          (generate-abc)))
