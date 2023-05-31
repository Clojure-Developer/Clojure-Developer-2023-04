(ns otus-8.util-test
  (:require [clojure.test :refer :all]
            [otus-8.util :as sut]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]))

(defn fix-with-msgs
  [fix-name]
  (fn [t]
    (println "Start " fix-name)
    (t)
    (println "End " fix-name)))

(use-fixtures :once
  (fix-with-msgs :once-1))

(use-fixtures :each
  (fix-with-msgs :each-1)
  (fix-with-msgs :each-2))

(deftest test-cel->fahr
  (testing "Legal arguments."
    (is (= 212 (int (sut/cel->fahr 100))))
    (is (= 32 (int (sut/cel->fahr 0))))

    (are [fahr cel] (= fahr (int (sut/cel->fahr cel)))
      212 100
      32 0))

  (testing "Illigal arguments."
    (is (thrown? Exception (sut/cel->fahr "100")))
    (is (thrown-with-msg? Exception
                          #"temperature must be a real number"
                          (sut/cel->fahr "100")))))

(deftest ^:integration test-a
  (is (= 0 0)))
