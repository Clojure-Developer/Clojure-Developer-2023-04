(ns url-shortener.core-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [url-shortener.core :as sut]))

(def symbols "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")

(def base-number (count symbols))

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

(deftest test-get-idx
  (testing "random inputs"
    (do-test-on-random-int-inputs 100 #(quot % base-number) (comp int sut/get-idx)))

  (testing "ranged-inputs"
    (are [ranged-inputs expected] (every? #(= expected %) (map sut/get-idx ranged-inputs))
          (range 0 base-number) 0.0
          (range (inc base-number) (* 2 base-number)) 1.0
          (range (inc (* 2 base-number)) (* 3 base-number)) 2.0
          (range (inc (* 3 base-number)) (* 4 base-number)) 3.0
          (range (inc (* 4 base-number)) (* 5 base-number)) 4.0
          (range (inc (* 5 base-number)) (* 6 base-number)) 5.0
          (range (inc (* 6 base-number)) (* 7 base-number)) 6.0
          (range (inc (* 7 base-number)) (* 8 base-number)) 7.0
          (range (inc (* 8 base-number)) (* 9 base-number)) 8.0
          (range (inc (* 9 base-number)) (* 10 base-number)) 9.0
          (range (inc (* 10 base-number)) (* 11 base-number)) 10.0
          (range (inc (* 11 base-number)) (* 12 base-number)) 11.0
          (range (inc (* 12 base-number)) (* 13 base-number)) 12.0
          (range (inc (* 13 base-number)) (* 14 base-number)) 13.0))

  (testing "fixed-inputs"
    (are [input expected] (= expected (sut/get-idx input))
          5432345 87618.0
          12312312 198585.0
          289328932 4666595.0
          Integer/MAX_VALUE 3.4636833E7))

  (testing "incorrect input"
    (do-test-on-incorrect-inputs sut/get-idx "123" "" nil [] [123] {})))

(deftest test-get-symbol-by-idx
  (testing "random inputs"
    (do-test-on-random-int-inputs 100 #(get symbols (rem % base-number)) sut/get-symbol-by-idx))

  (testing "incorrect input"
    (do-test-on-incorrect-inputs sut/get-symbol-by-idx "123" "" nil [] [123] {})))

(deftest test-id->url
  (testing "range from base number to double base number"
    (let [got (map sut/id->url (range 0 (* 2 base-number)))
          expected (concat
                     (map str (cons nil (rest symbols)))
                     (map #(str "b" %) symbols))]
      (is (= expected got))))

  (testing "some fixed input"
    (are [url id] (= url (sut/id->url id))
          ""  0
          "9" 61
          "ba" 62
          "bb" 63
          "b9" 123
          "rbv" 65431
          "gnQwY" 91919142)))

(deftest test-url->id
  (testing "some fixed input"
    (are [id url] (= id (sut/url->id url))
          0 ""
          61 "9"
          62 "ba"
          63 "bb"
          123 "b9"
          65431 "rbv"
          91919142 "gnQwY")))

(deftest test-id-to-url-and-back
  (testing "random inputs"
    (let [inputs (rand-integers 100)
          urls (map sut/id->url inputs)
          idx (map sut/url->id urls)]
      (is (= inputs idx) (format "fail for input = %s" (apply list inputs))))))

(deftest test-short-url-and-back
  (testing "positive cases"
    (let [db (io/as-file "test-short-url-and-back.txt")]
      (are [original] (= original (->> original
                                       (sut/get-shorten-url db)
                                       (sut/get-find-long-url db)))
        "https://clojure.org/about/rationale"
        "https://otus.ru/lessons/clojure-developer/"
        "https://google.com"))))

