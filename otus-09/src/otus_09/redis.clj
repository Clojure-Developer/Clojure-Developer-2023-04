(ns otus-09.redis
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))


(defn handler [command]
  )

;; 

(deftest basic
  (is (= "+PONG\r\n" (handler ["PING"])))
  (is (= "+Hello\r\n" (handler ["ECHO" "Hello"]))))

(deftest command-case
  (is (= "+Hello\r\n" (handler ["echo" "Hello"]))))

(deftest get-and-set
  (is (= "+(nil)\r\n" (handler ["GET" "nonexistent"])))
  (is (= "+OK\r\n" (handler ["SET" "x" "42"]))))

(run-tests 'otus-09.redis)
