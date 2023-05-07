(ns otus-02.homework.palindrome-test
  (:require
   [clojure.test :refer :all]
   [otus-02.homework.palindrome :as sut]))


(deftest palindrome-test
  (testing "should recognize as palindromes"
    (is (sut/is-palindrome "civic"))
    (is (sut/is-palindrome "tattarrattat"))
    (is (sut/is-palindrome "taco cat"))
    (is (sut/is-palindrome "no lemon, no melon"))
    (is (sut/is-palindrome "Eva, can I see bees in a cave?"))
    (is (sut/is-palindrome "Was it a cat I saw?")))

  (testing "should not recognize as palindromes"
    (is (not (sut/is-palindrome "civics")))
    (is (not (sut/is-palindrome "They all have one thing")))
    (is (not (sut/is-palindrome "knock on the door")))))
