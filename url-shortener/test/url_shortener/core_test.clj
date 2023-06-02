(ns url-shortener.core-test
  (:require [clojure.test :refer :all]
            [url-shortener.core :as sut]
            [clojure.string :as str]
            [clojure.java.io :as io]))


(defn fix-test-file[]
  (fn [t]
    (let [old-file (try 
                     (slurp sut/db)
                     (catch java.io.FileNotFoundException e nil))]
      (spit sut/db "https://clojuredocs.org/\nhttps://www.google.com\nhttps://clojure.org\n")
      (t)
      (if (nil? old-file)
        (io/delete-file sut/db) ; #TODO Добавить удаление файла)
        (spit sut/db old-file)))))

(defn fix-redef-println []
  (fn [t]
    (with-redefs [println (fn [& args] (str (str/join " " args)))]
      (t))))
      
(use-fixtures :once
  (fix-test-file))  

(use-fixtures :each
  (fix-redef-println))


(deftest get-idx-test
  (testing "Correct args"
    (are [idx i] = idx (sut/get-idx i)
         0.0 0 0.0 44 1.0 106 378.0 23456))
  (testing "Incorrect args"
    (is (thrown? Exception (sut/get-idx "123")))
    (is (thrown? Exception (sut/get-idx [1234])))
    (is (thrown? Exception (sut/get-idx nil)))))
     
((deftest get-symbol-by-idx-test
   (testing "Correct args"
     (are [s i] = s (sut/get-symbol-by-idx)
          \d 3 \S 44 \S 106 \E 1456))
   (testing "Incorrect args"
     (is (thrown? Exception (sut/get-symbol-by-idx "123")))
     (is (thrown? Exception (sut/get-symbol-by-idx [1234])))
     (is (thrown? Exception (sut/get-symbol-by-idx nil))))))
     
(deftest id->url-test
  (testing "Correct args"
    (are [url id] = url (sut/id->url)
         "H" 33 "bS" 106 "ZXP0" 12345678))
  (testing "Incorrect args"
     (is (thrown? Exception (sut/id->url "123")))
     (is (thrown? Exception (sut/id->url '(123))))
    ;; Текущая версия этот тест не проходит.
    ;; Но он должен быть на мой взгляд.
    ;; Поведение id->url и url->id должно быть одинаковым
     (is (thrown? Exception (sut/id->url nil)))))
         
(deftest url->id-test
  (testing "Correct args"
    (are [id url] = id (sut/url->id)
         131 "ch" 89925 "xyz" 15264777 "bcdef"))
  (testing "Incorrect args"
    (is (thrown? Exception (sut/url->id 123)))
    (is (thrown? Exception (sut/url->id "абвг")))
    (is (thrown? Exception (sut/url->id nil)))))

(deftest url<->id-test
  (testing "id->url->id"
    (are [id] = id (sut/url->id (sut/id->url id))
         1 12 123 123456789))
  (testing "url->id->url"
    (are [url]  = url (sut/id->url (sut/url->id url))
         "a" "fg" "xyz" "ffr3f3dd")))

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
    ;; Тут тест тоже падает. Но мне кажется функция должна nil возвращать
    ;; Мало ли что пользователь введет
    (is (nil? (sut/find-long-url "http://otus-url/h")))))
          

(deftest main-test
   (testing "command"
     (is (= "Your short URL: http://otus-url/e"
            (sut/-main "shorten" "https://clojure.org/about/rationale")))
     (is (= "Your original URL: https://clojuredocs.org/"
            (sut/-main "find" "http://otus-url/b"))))
  (testing "Unknow command"
     (is (= "Unknown command: search"
            (sut/-main "search" "http://otus-url/b")))))
         
 