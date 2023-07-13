(ns otus-16.homework-test
  (:require [clojure.test :refer :all]
            [otus-16.homework :as sut]))


(def stub-row "66.249.68.12 - - [20/Sep/2020:22:11:20 +0000] \"GET /%D0%BC%D0%B0%D0%BB%D0%B5%D0%BD%D1%8C%D0%BA%D0%B8%D0%B9-%D0%BC%D0%B0%D0%BB%D1%8C%D1%87%D0%B8%D0%BA-%D0%BF%D0%BE-%D0%B8%D0%BC%D0%B5%D0%BD%D0%B8-%D0%9D%D1%83%D1%80%D0%B1%D0%B5%D0%BA-%D0%B6%D0%B8%D0%BB-%D0%B2-%D0%BD%D0%B5%D0%B1%D0%BE%D0%BB%D1%8C%D1%88%D0%BE%D0%B9/?p=2 HTTP/1.1\" 304 0 \"-\" \"Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.110 Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)\" \"-\"")

(deftest parse-log-test
  (testing "parse-log"
    (is (= {:ip "66.249.68.12",
           :user-name "-",
           :date-time "[20/Sep/2020:22:11:20 +0000]",
           :request
           "GET /%D0%BC%D0%B0%D0%BB%D0%B5%D0%BD%D1%8C%D0%BA%D0%B8%D0%B9-%D0%BC%D0%B0%D0%BB%D1%8C%D1%87%D0%B8%D0%BA-%D0%BF%D0%BE-%D0%B8%D0%BC%D0%B5%D0%BD%D0%B8-%D0%9D%D1%83%D1%80%D0%B1%D0%B5%D0%BA-%D0%B6%D0%B8%D0%BB-%D0%B2-%D0%BD%D0%B5%D0%B1%D0%BE%D0%BB%D1%8C%D1%88%D0%BE%D0%B9/?p=2 HTTP/1.1",
           :response "304",
           :size "0",
           :referer "-",
           :user-agent
           "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.110 Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)"}
           (sut/parse-log stub-row)))))
