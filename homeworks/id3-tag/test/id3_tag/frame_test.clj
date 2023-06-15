(ns id3-tag.frame-test
  (:require [clojure.test :refer :all]
            [id3-tag.frame :as sut]))

(deftest utils-test
  (testing "full-size?"
    (are [ans tmap] (= ans (sut/full-size? tmap))
      true {:data [1 2 3 4 5] :size 5}
      false {:data [1 2 3 4 5] :size 6}))
  (testing "calc-length"
    (are [size v] (= size (sut/calc-length v))
      144 [0 0 1 16]
      12 [0 0 0 12]))
  (testing "with-ext-header?"
    (are [ans flag] (= ans (sut/with-ext-header? flag))
      true 2r01000000
      true 2r01000011
      true 2r11111111
      false 2r00000000
      false 2r10111111)))

(deftest parse-step-test
      (testing "parse-step"
        (are  [ans tmap] (= ans (sut/parse-step tmap))
          {:data [], :parse-step :extra-header, :size 10}
          {:data [73 68 51 4 0 64 0 0 1 16], :parse-step :header, :size 10}

          {:data [], :parse-step :frame-header, :size 10}
          {:data [0 0 0 12 1 32 5 14 55 54 57 79], :parse-step :skip-ex-header, :size 12}
          
          {:data [], :parse-step :frame, :size 10, :frame "TCON"}
          {:data [84 67 79 78 0 0 0 10 0 0], :parse-step :frame-header, :size 10}
          
          {:data [], :parse-step :frame-header, :size 10, :tag ["TCON" "Cinematic"]}
          {:data [0 67 105 110 101 109 97 116 105 99], :parse-step :frame, :size 10, :frame "TCON"})))
          
          
          
          
          
          
          
          
          


   
               
