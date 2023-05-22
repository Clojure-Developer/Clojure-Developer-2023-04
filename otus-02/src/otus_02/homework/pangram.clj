(ns otus-02.homework.pangram
  (:require [clojure.string :as str]))

(defn is-char [ch] (Character/isLetter ch))

(defn is-pangram [test-string]
  (let
    [tested-char-set (set (filter is-char (str/lower-case test-string)))
     all-chars (set (map char (range (int \a) (inc (int \z)))))]
    (= tested-char-set all-chars)))
