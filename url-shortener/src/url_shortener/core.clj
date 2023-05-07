(ns url-shortener.core
  (:require [clojure.string :as string]))


(def symbols
  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")


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


(comment
 (id->url 12345))


;; =============================================================================
;; String -> Number
;; =============================================================================

(def a-code (int \a))
(def z-code (int \z))
(def A-code (int \A))
(def Z-code (int \Z))
(def code-0 (int \0))
(def code-9 (int \9))


(defn url->id [url]
  (let [url-symbols (seq url)]
    (reduce
     (fn [id symbol]
       (let [char-code (int symbol)]
         (cond (<= a-code char-code z-code)
               (+ (* id 62)
                  (- char-code a-code))

               (<= A-code char-code Z-code)
               (+ (* id 62)
                  (- char-code A-code)
                  26)

               (<= code-0 char-code code-9)
               (+ (* id 62)
                  (- char-code code-0)
                  52))))
     0
     url-symbols)))


(comment
 (url->id "dnh"))
