(ns url-shortener.core
  (:require
   [clojure.string :as string]))

(def symbols
  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")

(def alphabet-length (count symbols))


(defn- get-idx [i]
  (Math/floor (/ i alphabet-length)))


(defn- get-symbol-by-idx [i]
  (get symbols (rem i alphabet-length)))


(defn- int->key [id]
  (let [idx-sequence  (iterate get-idx id)
        valid-idxs    (take-while #(> % 0) idx-sequence)
        code-sequence (map get-symbol-by-idx valid-idxs)]
    (string/join (reverse code-sequence))))


(defn url->key [url]
  (int->key (java.lang.Integer/toUnsignedLong (hash url))))
