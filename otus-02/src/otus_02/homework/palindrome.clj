(ns otus-02.homework.palindrome
  (:require [clojure.string :as cs]))

(defn- bad-char? [c]
  (boolean (some #{\space \? \,} [c])))

(defn- bad-char-to-nil [c]
  (when-not (bad-char? c)
    c))
(defn- rm-bad-chars [s]
  (apply str (map bad-char-to-nil s)))
(defn is-palindrome [test-string]
  (let [s (cs/lower-case (rm-bad-chars test-string))
        rs (cs/reverse s)]
    (= s rs)))