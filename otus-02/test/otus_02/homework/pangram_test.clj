(ns otus-02.homework.pangram-test
  (:require
   [clojure.test :refer :all]
   [otus-02.homework.pangram :as sut]))


(deftest is-pangram-test
  (is (sut/is-pangram "The quick brown fox jumps over the lazy dog"))
  (is (sut/is-pangram "Pack my box with five dozen liquor jugs"))
  (is (sut/is-pangram "How vexingly quick daft zebras jump"))
  (is (sut/is-pangram "Waltz, bad nymph, for quick jigs vex"))
  (is (sut/is-pangram "Sphinx of black quartz, judge my vow"))
  (is (sut/is-pangram "Jackdaws love my big sphinx of quartz"))

  (is (not (sut/is-pangram "The quick brown fox jumps over the dog")))
  (is (not (sut/is-pangram "Keep in mind that"))))
