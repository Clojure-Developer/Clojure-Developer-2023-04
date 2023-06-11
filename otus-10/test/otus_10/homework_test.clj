(ns otus-10.homework-test
  (:require [otus-10.homework :as sut]
            [clojure.test :refer [deftest testing is are]]))

(deftest byte-seq->int-test
  (testing "should convert positive numbers"
    (are [expected actual] (= expected (sut/byte-seq->int actual))
      1 [0 0 0 1]
      256 [0 0 2 0])))

(deftest byte-seq->str-test
  (testing "should convert byte sequence to string"
    (is (= "Hello, world!" (sut/byte-seq->str [72 101 108 108 111 44 32 119 111 114 108 100 33] "UTF-8")))))

(deftest decode-frame-text-test
  (testing "should decode ISO-8859-1 frame"
    (are [expected actual] (= expected (sut/decode-frame-text actual))
      "Test" [0 84 101 115 116]
      "world" [0 119 111 114 108 100])))

(deftest decode-tags-test
  (testing "should decode album tag"
    (let [album-bytes-tag [84 65 76 66]]
     (is (= {:album "Test"} (sut/decode-tags album-bytes-tag [0 84 101 115 116])))
     (is (= {:album "world"} (sut/decode-tags album-bytes-tag [0 119 111 114 108 100])))))

  (testing "should decode title tag"
    (let [title-bytes-tag [84 73 84 50]]
      (is (= {:title "Test"} (sut/decode-tags title-bytes-tag [0 84 101 115 116])))
      (is (= {:title "world"} (sut/decode-tags title-bytes-tag [0 119 111 114 108 100])))))

  (testing "should decode year tag"
    (let [year-bytes-tag [84 89 69 82]]
      (is (= {:year 1997} (sut/decode-tags year-bytes-tag [0 49 57 57 55])))))

  (testing "should decode genre tag"
    (let [genre-bytes-tag [84 67 79 78]]
      (is (= {:genre "Test"} (sut/decode-tags genre-bytes-tag [0 84 101 115 116])))
      (is (= {:genre "world"} (sut/decode-tags genre-bytes-tag [0 119 111 114 108 100]))))))

(deftest ^:integration decode-build-info-test
  (testing "should build mp3 file info"
    (let [mp3-file-info (sut/build-info "test/otus_10/resources/test.mp3")]
      (is (= {:album "YouTube Audio Library"
              :title "Impact Moderato"
              :year 2014
              :genre "Cinematic"} mp3-file-info)))))
