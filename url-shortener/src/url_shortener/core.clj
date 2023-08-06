(ns url-shortener.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]))


(def symbols
  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")


(def host
  "http://otus-url/")


(def db
  (io/as-file "url.txt"))


(defn init-storage []
  (when-not (.exists db)
    (.createNewFile db)))


;; =============================================================================
;; Number -> String
;; =============================================================================

(defn get-idx [i]
  (Math/floor (/ i 62)))


(defn get-symbol-by-idx [i]
  (get symbols (rem i 62)))


(defn id->url [id]
  (let [idx-sequence  (iterate get-idx id)
        valid-idxs    (take-while #(> % 0) idx-sequence)
        code-sequence (map get-symbol-by-idx valid-idxs)]
    (string/join (reverse code-sequence))))



;; =============================================================================
;; String -> Number
;; =============================================================================


(defn url->id [url]
  (let [url-symbols (seq url)]
    (reduce
     (fn [id symbol]
       (+ (* id 62)
          (string/index-of symbols symbol)))
     0
     url-symbols)))



;; =============================================================================
;; Application API
;; =============================================================================


(defn shorten-url [url]
  (spit db url :append true)
  (spit db \newline :append true)

  (with-open [file (io/reader db)]
    (let [url-id (count (line-seq file))
          hash   (id->url url-id)]
      (str host hash))))


(defn find-long-url [hash]
  (let [line-number (url->id hash)]
    (with-open [file (io/reader db)]
      (let [original-url (nth (line-seq file) (dec line-number))]
        original-url))))


(defn get-all-urls []
  (with-open [file (io/reader db)]
    (-> (line-seq file)
        vec)))
