(ns otus-04.homework.scramblies-test
  (:require
   [clojure.test :refer :all]
   [otus-04.homework.scramblies :refer [scramble?]]))

(deftest scramble?-test
  (is (scramble? "rkqodlw" "world"))
  (is (not (scramble? "catacomb" "aaa")))
  (is (scramble? "cedewaraaossoqqyt" "codewars"))
  (is (not (scramble? "katas" "steak"))))
