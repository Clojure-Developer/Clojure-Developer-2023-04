(ns url-shortener.core-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [url-shortener.core :as sut]))

(def symbols "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")

(def base-number (count symbols))

(defn rand-n-integers [n]
  (repeatedly n #(rand-int (Integer/MAX_VALUE))))
(defn do-test-on-random-int-inputs
  [n expected-fn got-fn]
  (let [random-inputs (rand-n-integers n)
        expected (map expected-fn random-inputs)
        got (map got-fn random-inputs)]
    (is (= expected got) (format "fail for input = %s" (apply list random-inputs)))))

(defmacro do-test-on-incorrect-inputs [test-fn & inputs]
  `(are [input] (~'thrown? Exception (~test-fn input)) ~@inputs))

(deftest test-get-idx
  (testing "random inputs"
    (do-test-on-random-int-inputs 100 #(quot % base-number) (comp int sut/get-idx)))

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
    (let [inputs (rand-n-integers 100)
          urls (map sut/id->url inputs)
          idx (map sut/url->id urls)]
      (is (= inputs idx) (format "fail for input = %s" (apply list inputs))))))

(deftest test-short-url-and-back
  (testing "positive cases"
    (let [db (io/as-file "test-short-url-and-back.txt")]
      (are [original]
        (= original
           (->> original
                (sut/do-shorten-url db)
                (sut/do-find-long-url db)))
        "https://clojure.org/about/rationale"
        "https://otus.ru/lessons/clojure-developer/"
        "https://google.com"))))

