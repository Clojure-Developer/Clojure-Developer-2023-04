(ns test-utils
  (:require [clojure.test :refer :all]))

(defn rand-integers
  ([]
   (repeatedly #(rand-int Integer/MAX_VALUE)))
  ([n]
   (take n (rand-integers))))

(defn do-test-on-random-int-inputs
  [n expected-fn got-fn]
  (let [random-inputs (rand-integers n)
        expected (map expected-fn random-inputs)
        got (map got-fn random-inputs)]
    (is (= expected got) (format "fail for input = %s" (apply list random-inputs)))))

(defmacro do-test-on-incorrect-inputs [test-fn & inputs]
  `(are [input#] (~'thrown? Exception (~test-fn input#)) ~@inputs))
