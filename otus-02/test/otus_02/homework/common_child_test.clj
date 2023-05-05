(ns otus-02.homework.common-child-test
  (:require
   [clojure.test :refer :all]
   [otus-02.homework.common-child :as sut]))


(deftest common-child-test

  (is (= (sut/common-child-length "SHINCHAN" "NOHARAAA")
         3))

  (is (= (sut/common-child-length "HARRY" "SALLY")
         2))

  (is (= (sut/common-child-length "AA" "BB")
         0))

  (is (= (sut/common-child-length "ABCDEF" "FBDAMN")
         2)))
