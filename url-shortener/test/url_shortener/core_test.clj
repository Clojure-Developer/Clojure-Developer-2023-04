(ns url-shortener.core-test
  (:require [clojure.test :refer :all]
            [url-shortener.core :as sut]
            [url-shortener.test-util :refer :all]))


(use-fixtures :once
  (fix-test-file sut/db)
  fix-redef-println)

(deftest get-idx-test
  (testing "Correct args"
    (are [idx i] (= idx (sut/get-idx i))
      0.0 0 
      0.0 44 
      1.0 106 
      378.0 23456))
  
  (testing "Incorrect args"
    (are [id] (thrown? Exception (sut/get-idx id))
      "123"
      [123]
      nil)))


(deftest get-symbol-by-idx-test
   (testing "Correct args"
     (are [s i] (= s (sut/get-symbol-by-idx i))
       \d 3
       \S 44 
       \S 106 
       \E 1456))
  
   (testing "Incorrect args"
     (are [idx] (thrown? Exception (sut/get-symbol-by-idx idx))
       "123"
       [123]
       nil)))
     

(deftest id->url-test
  (testing "Correct args"
    (are [url id] (= url (sut/id->url id))
      "H" 33
      "bS" 106 
      "ZXP0" 12345678))
  
  (testing "Incorrect args"
    (are [url] (thrown? Exception (sut/id->url url))
      "123"
      '(123)
      nil)))
    
         
(deftest url->id-test
  (testing "Correct args"
    (are [id url] (= id (sut/url->id url))
      131 "ch" 
      89925 "xyz" 
      15264777 "bcdef"
      0 nil))
  
  (testing "Incorrect args"
    (are [url] (thrown? Exception (sut/url->id url))
      "абвг"
      123)))


(deftest url<->id-test
  (testing "id->url->id"
    (are [id] (= id (sut/url->id (sut/id->url id)))
      1 
      12
      123 
      123456789))
  
  (testing "url->id->url"
    (are [url] (= url (sut/id->url (sut/url->id url)))
      "b"
      "fg" 
      "xyz" 
      "ffr3f3dd")))

(deftest shorten-url-test
  (testing "Correct argument"
    (let [old-file (slurp sut/db)]
      (is (= "Your short URL: http://otus-url/f"
             (sut/shorten-url "https://otus.ru/")))
      (is (= (str old-file "https://otus.ru/\n")
             (slurp sut/db))))))

(deftest find-long-url-test
  (testing "Correct args"
    (is (= "Your original URL: https://clojuredocs.org/"
           (sut/find-long-url "http://otus-url/b"))))

  (testing "Incorrect args"
    (is (thrown? Exception (sut/find-long-url "http://otus-url/h")))))
          

(deftest main-test
  (testing "command"
    (is (= "Your short URL: http://otus-url/e"
           (sut/-main "shorten" "https://clojure.org/about/rationale")))
    (is (= "Your original URL: https://clojuredocs.org/"
           (sut/-main "find" "http://otus-url/b"))))
  (testing "Unknow command"
    (is (= "Unknown command: search"
           (sut/-main "search" "http://otus-url/b")))))
         
 