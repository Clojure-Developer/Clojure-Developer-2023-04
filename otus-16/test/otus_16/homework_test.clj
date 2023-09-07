(ns otus-16.homework-test
  (:require [clojure.test :refer :all]
            [otus-16.homework :as sut]
            [clojure.string :as str]
            [clojure.set :as set])
  (:import [java.io File]))

(deftest test-parse-time
  (testing "Time parsing"
    (are [time s] (= time (sut/parse-time s))
         #inst "1970-01-01T00:00:00.000-00:00" "01/Jan/1970:00:00:00 +0000"
         #inst "2003-02-01T03:58:06.000-00:00" "01/Feb/2003:04:05:06 +0007")))

(deftest test-urldecode
  (testing "URL decoding"
    (are [decoded url] (= decoded (sut/urldecode url))
         "012"        "%30%31%32"
         "Проверка"   "%d0%9f%d1%80%d0%be%d0%b2%d0%b5%d1%80%d0%ba%d0%b0"
         "%A%B%C"     "%A%B%C"
         nil          nil)))

(deftest test-parse-combined-log-line
  (testing "Log line parsing"
    (are [parsed line] (= parsed (sut/parse-combined-log-line line))
         {:host "1.2.3.4",
          :user nil,
          :time #inst "1970-01-01T00:00:00.000-00:00",
          :method "GET"
          :url "/index.html"
          :http-ver "1.1"
          :status 200,
          :size 256,
          :referrer "https://example.com",
          :user-agent "Mozilla/5.0"}
         "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /index.html HTTP/1.1\" 200 256 \"https://example.com\" \"Mozilla/5.0\" \"-\""

         {:host "1.2.3.4",
          :user "username",
          :time #inst "2003-02-01T03:58:06.000-00:00",
          :method "GET"
          :url "/index.html"
          :http-ver "1.1"
          :status 200,
          :size nil,
          :referrer "https://example.com/Проверка",
          :user-agent "Mozilla/5.0"}
         "1.2.3.4 - username [01/Feb/2003:04:05:06 +0007] \"GET /index.html HTTP/1.1\" 200 - \"https://example.com/%d0%9f%d1%80%d0%be%d0%b2%d0%b5%d1%80%d0%ba%d0%b0\" \"Mozilla/5.0\" \"-\""

         {:host "1.2.3.4",
          :user "username",
          :time #inst "1970-01-01T00:00:00.000-00:00",
          :method "GET"
          :url "/index.html"
          :http-ver "1.1"
          :status 200,
          :size nil,
          :referrer nil,
          :user-agent nil}
         "1.2.3.4 - username [01/Jan/1970:00:00:00 +0000] \"GET /index.html HTTP/1.1\" 200 - \"-\" \"-\" \"-\"")))

(deftest test-get-init
  (testing "Getting initial values from slices hash-map"
    (are [init slices] (= init (sut/get-init slices))
         {:size 0}
         {:size {:init 0}}

         {:size 0 :urls []}
         {:size {:init 0} :urls {:init []}})))

(deftest test-parse-chunk
  (let [sample-chunk
        ["1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /index.html HTTP/1.1\" 200 256 \"https://example.com\" \"Mozilla/5.0\" \"-\""
         "1.2.3.4 - username [01/Feb/2003:04:05:06 +0007] \"GET /robots.txt HTTP/1.1\" 200 - \"https://example.com\" \"Mozilla/5.0\" \"-\""
         "1.2.3.4 - username [01/Jan/1970:00:00:00 +0000] \"GET /index.html HTTP/1.1\" 200 256 \"-\" \"-\" \"-\""]]
    (testing "Chunk parsing"
      (are [data conditions] (= data (sut/parse-chunk sut/slices conditions sample-chunk))
           {:total-bytes 512,
            :bytes-by-url {"/index.html" 512, "/robots.txt" 0},
            :urls-by-referrer {"https://example.com" 2, nil 1}}
           {}

           {:total-bytes 256,
            :bytes-by-url {"/index.html" 256},
            :urls-by-referrer {nil 1}}
           {:url "/index.html" :referrer nil}

           {:total-bytes 0,
            :bytes-by-url {"/robots.txt" 0},
            :urls-by-referrer {"https://example.com" 1}}
           {:url "/robots.txt"}))))

(deftest test-merge-chunks
  (let [slices {:param1 {:init 0 :merge-fn +}
                :param2 {:init [] :merge-fn (fn [merged next] (reduce conj merged next))}}]
    (testing "Chunk merging"
      (are [merged chunks] (= merged (sut/merge-chunks slices chunks))
           {:param1 100 :param2 [1 2 3]}
           [{:param1 10 :param2 [1]}
            {:param1 40 :param2 [2]}
            {:param1 50 :param2 [3]}]))))

(def sample-file-data
  ["1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /index.html HTTP/1.1\" 200 100 \"https://example.com\" \"Mozilla/5.0\" \"-\""
   "1.2.3.4 - - [01/Feb/2003:04:05:06 +0007] \"GET /robots.txt HTTP/1.1\" 200 200 \"https://example.com\" \"Mozilla/5.0\" \"-\""
   "1.2.3.4 - - [01/Feb/2003:04:05:06 +0007] \"GET /page1.html HTTP/1.1\" 200 1000 \"-\" \"Arachne/1.0\" \"-\""
   "1.2.3.4 - - [01/Feb/2003:04:05:06 +0007] \"GET /page2.html HTTP/1.1\" 200 1000 \"-\" \"Arachne/1.0\" \"-\""
   "1.2.3.4 - - [01/Feb/2003:04:05:06 +0007] \"GET /page3.html HTTP/1.1\" 200 1000 \"-\" \"Arachne/1.0\" \"-\""
   "1.2.3.4 - - [01/Feb/2003:04:05:06 +0007] \"GET /page4.html HTTP/1.1\" 200 1000 \"-\" \"Arachne/1.0\" \"-\""
   "1.2.3.4 - - [01/Feb/2003:04:05:06 +0007] \"GET /news/?page=1 HTTP/1.1\" 200 2000 \"https://example.com/index.html\" \"Lynx\" \"-\""
   "1.2.3.4 - - [01/Feb/2003:04:05:06 +0007] \"GET /news/?page=2 HTTP/1.1\" 200 2000 \"https://example.com/index.html\" \"Lynx\" \"-\""
   "1.2.3.4 - - [01/Feb/2003:04:05:06 +0007] \"GET /news/?page=3 HTTP/1.1\" 200 2000 \"https://example.com/index.html\" \"Lynx\" \"-\""
   "1.2.3.4 - - [01/Feb/2003:04:05:06 +0007] \"GET /news/?page=4 HTTP/1.1\" 200 2000 \"https://example.com/index.html\" \"Lynx\" \"-\""
   "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /etc/passwd HTTP/1.1\" 404 - \"-\" \"-\" \"-\""
   "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /../../../../../../../etc/passwd HTTP/1.1\" 200 3000 \"-\" \"-\" \"-\""
   "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /.git HTTP/1.1\" 404 - \"-\" \"-\" \"-\""
   "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /admin HTTP/1.1\" 404 - \"-\" \"-\" \"-\""
   "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /.htaccess HTTP/1.1\" 404 - \"-\" \"-\" \"-\""
   "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /phpinfo.php HTTP/1.1\" 404 - \"-\" \"-\" \"-\""
   "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /backup HTTP/1.1\" 404 - \"-\" \"-\" \"-\""
   "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /backup.zip HTTP/1.1\" 404 - \"-\" \"-\" \"-\""
   "1.2.3.4 - - [01/Jan/1970:00:00:00 +0000] \"GET /backup.tar.gz HTTP/1.1\" 404 - \"-\" \"-\" \"-\""
   "1.2.3.4 - - [31/Dec/1999:23:59:59 +0003] \"POST / HTTP/1.1\" 503 - \"-\" \"curl\" \"-\""
   "1.2.3.4 - - [31/Dec/1999:23:59:59 +0003] \"POST / HTTP/1.1\" 503 - \"-\" \"curl\" \"-\""
   "1.2.3.4 - - [31/Dec/1999:23:59:59 +0003] \"DELETE / HTTP/1.1\" 500 - \"-\" \"xh\" \"-\""])

(deftest test-parse-file
  (let [tempfile (str (File/createTempFile "test" ".tmp"))]
    (spit tempfile (str/join "\n" sample-file-data))

    (testing "Log file parsing with default slices and 5 lines per chunk."
      (let [data (sut/parse-file tempfile 5 sut/slices {})]
        (is (= 15300 (:total-bytes data)))
        (is (= 20 (count (:bytes-by-url data))))
        (is (= 3 (count (:urls-by-referrer data))))))

    (testing "Log file parsing with default slices and 100 lines per chunk."
      (let [data (sut/parse-file tempfile 100 sut/slices {})]
        (is (= 15300 (:total-bytes data)))
        (is (= 20 (count (:bytes-by-url data))))
        (is (= 3 (count (:urls-by-referrer data))))))

    (testing "Log file parsing with default slices and conditions."
      (is (= {:total-bytes 0, :bytes-by-url {"/" 0}, :urls-by-referrer {nil 3}}
             (sut/parse-file tempfile 5 sut/slices {:url "/"})))

      (is (= {:total-bytes 300,
              :bytes-by-url {"/index.html" 100, "/robots.txt" 200},
              :urls-by-referrer {"https://example.com" 2}}
             (sut/parse-file tempfile 5 sut/slices {:referrer "https://example.com"})))

      (is (= {:total-bytes 200,
              :bytes-by-url {"/robots.txt" 200},
              :urls-by-referrer {"https://example.com" 1}}
             (sut/parse-file tempfile 5 sut/slices {:url "/robots.txt" :referrer "https://example.com"}))))

    (testing "Log file parsing with custom slices and conditions"
      (is (= {:total-size 15300}
             (sut/parse-file tempfile 10 {:total-size {:init 0
                                                       :acc-fn (fn [old rec] (+ old (or (:size rec) 0)))
                                                       :merge-fn +}} 
                             {:host "1.2.3.4"})))

      (is (= {:codes #{"DELETE" "POST" "GET"}}
             (sut/parse-file tempfile 10  {:codes {:init #{}
                                                   :acc-fn (fn [old rec] (conj old (:method rec)))
                                                   :merge-fn set/union}}
                             {}))))))


(defn make-tempfiles
  "Разбивает набор данных на чанки и записывает их во временные файлы.
  Возвращает список имен файлов."
  [data chunk-size]
  (when (seq data)
    (let [tempfile (str (java.io.File/createTempFile "test" ".tmp"))
          chunk (take chunk-size data)]
      (spit tempfile (str/join "\n" chunk))
      (lazy-seq (cons tempfile (make-tempfiles (drop chunk-size data) chunk-size))))))

(deftest test-parse-files
  (let [tempfiles (make-tempfiles sample-file-data 10)]

    (testing "Multiple log files parsing with default slices and 5 lines per chunk."
      (let [data (sut/parse-files tempfiles 5 sut/slices {})]
        (is (= 15300 (:total-bytes data)))
        (is (= 20 (count (:bytes-by-url data))))
        (is (= 3 (count (:urls-by-referrer data))))))

    (testing "Multiple log files parsing with default slices and 100 lines per chunk."
      (let [data (sut/parse-files tempfiles 100 sut/slices {})]
        (is (= 15300 (:total-bytes data)))
        (is (= 20 (count (:bytes-by-url data))))
        (is (= 3 (count (:urls-by-referrer data))))))

    (testing "Multiple log files parsing with default slices and conditions."
      (is (= {:total-bytes 0, :bytes-by-url {"/" 0}, :urls-by-referrer {nil 3}}
             (sut/parse-files tempfiles 5 sut/slices {:url "/"})))

      (is (= {:total-bytes 300,
              :bytes-by-url {"/index.html" 100, "/robots.txt" 200},
              :urls-by-referrer {"https://example.com" 2}}
             (sut/parse-files tempfiles 5 sut/slices {:referrer "https://example.com"})))

      (is (= {:total-bytes 200,
              :bytes-by-url {"/robots.txt" 200},
              :urls-by-referrer {"https://example.com" 1}}
             (sut/parse-files tempfiles 5 sut/slices {:url "/robots.txt" :referrer "https://example.com"}))))

    (testing "Multiple log files parsing with custom slices and conditions."
      (is (= {:total-size 15300}
             (sut/parse-files tempfiles 10 {:total-size {:init 0
                                                       :acc-fn (fn [old rec] (+ old (or (:size rec) 0)))
                                                       :merge-fn +}} 
                             {:host "1.2.3.4"})))

      (is (= {:codes #{"DELETE" "POST" "GET"}}
             (sut/parse-files tempfiles 10  {:codes {:init #{}
                                                   :acc-fn (fn [old rec] (conj old (:method rec)))
                                                   :merge-fn set/union}}
                             {}))))))
