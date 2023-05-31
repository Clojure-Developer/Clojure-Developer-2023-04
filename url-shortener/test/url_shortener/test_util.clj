(ns url-shortener.test-util
  (:import [java.io File])
  (:require  [clojure.java.io :as io]
             [clojure.string :as str]
             [clojure.test :refer [is]]
             [url-shortener.core :as sut]))

(defn ^:export fix-with-url-db
  [t]
  (let [file (File/createTempFile "url" "txt")]
      (with-redefs [sut/db (io/as-file file)] (t))))

(defn ^:export assert-output [expected f]
    (let [out (str/trim (with-out-str (f)))]
      (is (= out expected))))
