(ns url-shortener.test-util
  (:import java.io.File)
  (:require  [clojure.java.io :as io]
             [url-shortener.core :as sut]))

(defn fix-with-url-db
  [t]
  (let [file (File/createTempFile "url" "txt")]
      (with-redefs [sut/db (io/as-file file)]
        (t))))
