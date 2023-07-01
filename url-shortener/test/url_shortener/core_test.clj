(ns url-shortener.core-test
  (:require [clojure.test :refer :all]
            [url-shortener.core :as sut]))

(defn fix-with-db
  "Fixture with clean mock db in temporary file."
  [t]
  (with-redefs [sut/db (java.io.File/createTempFile "test-" ".tmp")]
    (println "DB replaced with" (.getPath sut/db))
    (t)))

(use-fixtures :once fix-with-db)

(deftest test-get-idx
  (testing "Getting indexes"
    (are [idx i] (= idx (int (sut/get-idx i)))
          0   0
          0  10
         10 620
          1  63))
  (testing "Getting indexes, illegal input"
    (is (thrown? Exception (sut/get-idx "xxx")))))

(deftest test-get-symbol-by-idx
  (testing "Getting symbol by numeric index"
    (are [sym i] (= sym (sut/get-symbol-by-idx i))
         nil -1
          \a  0
          \a 62
          \9 61))
  (testing "Getting symbol by illegal index"
    (is (thrown? Exception (sut/get-symbol-by-idx "xxx")))))

(deftest test-url->id
  (testing "Getting id by short part"
    (are [id url] (= id (sut/url->id url))
          0 ""
          0 "a"
          0 "aaaaaa"
          1 "b"
          1 "aaaaab"
         61 "9"
       3843 "99"
       3844 "baa"))
  (testing "Getting id by illegal string"
    (is (thrown? Exception (sut/url->id "!!!")))))

(deftest test-id->url
  (testing "Getting short url part by id"
    (are [url id] (= url (sut/id->url id))
          ""     0
          "b"    1
         "99" 3843))
  (testing "Getting short url part by illegal id"
    (is (thrown? Exception (sut/id->url "!!!")))))

(deftest test-shorten-url
  (testing "Storing urls in db"
    (are [message url] (= message (with-out-str (sut/shorten-url url)))
         (str "Your short URL: " sut/host "b\n") "https://example.com/1"
         (str "Your short URL: " sut/host "c\n") "https://example.com/2"
         (str "Your short URL: " sut/host "d\n") "https://example.com/3")))

(deftest test-find-long-url
  (testing "Finding urls in db"
    (are [message short-url] (= message (with-out-str (sut/find-long-url short-url)))
         "Your original URL: https://example.com/1\n" (str sut/host "b")
         "Your original URL: https://example.com/2\n" (str sut/host "c")
         "Your original URL: https://example.com/3\n" (str sut/host "d")))
  (testing "Finding non-existent urls in db"
    (is (thrown? Exception (sut/find-long-url (str sut/host "x"))))
    (is (thrown? Exception (sut/find-long-url "!!!")))))

(deftest test--main
  (testing "Storing url in db and then finding it"
    (are [message args] (= message (with-out-str (apply sut/-main args)))
         (str "Your short URL: " sut/host "e\n") ["shorten" "https://example.com/4"]
         (str "Your original URL: https://example.com/4\n") ["find" (str sut/host "e")]))
  (testing "Finding non-existent url in db"
    (is (thrown? Exception (sut/-main "find" (str sut/host "x"))))
    (is (thrown? Exception (sut/-main "find" "!!!"))))
  (testing "Unknown command"
    (is (= "Unknown command: xxx\n" (with-out-str (sut/-main "xxx" ""))))))
