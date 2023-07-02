(ns otus-16.homework-test
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [otus-16.homework :as sut]))

(def test-input
  (str/join "\n" ["176.193.24.191 - - [19/Jul/2020:07:41:32 +0000] \"GET /new/ HTTP/2.0\" 200 15716 \"https://baneks.site/\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWeb\nKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36\" \"-\""
                  "176.193.24.191 - - [19/Jul/2020:07:41:32 +0000] \"GET /new/?p=1 HTTP/2.0\" 200 15716 \"https://baneks.site/\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWeb\nKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36\""
                  "176.193.24.191 - - [19/Jul/2020:07:41:35 +0000] \"GET /admin/ HTTP/2.0\" 302 0 \"-\" \"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, li\nke Gecko) Chrome/81.0.4044.138 Safari/537.36\""
                  "178.154.200.81 - - [19/Jul/2020:07:49:24 +0000] \"GET /-hello-my-name-is-Peter-without-s-but-there-is-no/ HTTP/1.1\" 200 11372 \"-\" \"Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)\""
                  "178.154.200.81 - - [19/Jul/2020:07:49:24 +0000] \"GET /-hello-my-name-is-Peter-without-s-but-there-is-no/ HTTP/1.1\" 200 11372 \"-\" \"Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)\""
                  "141.8.142.75 - - [20/Sep/2020:22:35:19 +0000] \"GET /%D0%BA%D0%BE%D1%80%D1%80%D0%B5%D1%81%D0%BF%D0%BE%D0%BD%D0%B4%D0%B5%D0%BD%D1%82-%D0%B2%D0%B5%D0%B4%D0%B5%D1%82-%D1%80%D0%B5%D0%BF%D0%BE%D1%80%D1%82%D0%B0%D0%B6-%D1%81-%D0%9A%D1%80%D0%B0%D1%81%D0%BD%D0%BE%D0%B9-%D0%BF%D0%BB%D0%BE%D1%89%D0%B0%D0%B4%D0%B8/?p=2 HTTP/1.1\" 200 9530 \"-\" \"Mozilla/5.0 (compatible; YandexBot/3.0; +http://yandex.com/bots)\""]))

(deftest test-solution
  (testing "without filters"
    (with-open [r (io/reader (char-array test-input))]
      (is (= (sut/solution [r]) 63706))))

  (testing "with-url-filter"
    (with-open [r (io/reader (char-array test-input))]
      (is (= (sut/solution [r] :url "/new/") 31432))))

  (testing "with-url-filter-and-not-exiting"
    (with-open [r (io/reader (char-array test-input))]
      (is (= (sut/solution [r] :url "/not-existing-url/") 0))))

  (testing "with-referrer-filter"
    (with-open [r (io/reader (char-array test-input))]
      (is (= (sut/solution [r] :referrer "-") 32274))))

  (testing "with-referrer-filter-and-not-existing"
    (with-open [r (io/reader (char-array test-input))]
      (is (= (sut/solution [r] :referrer "not-existing-referrer") 0))))

  (testing "with-url-and-referrer-filter"
    (with-open [r (io/reader (char-array test-input))]
      (is (= (sut/solution [r] :url "/-hello-my-name-is-Peter-without-s-but-there-is-no/"
                           :referrer "-") 22744))))

  (testing "with-url-and-referrer-filter-not-existing"
    (with-open [r (io/reader (char-array test-input))]
      (is (= (sut/solution [r] :url "unknown-url"
                           :referrer "unknown-referrer") 0)))))

