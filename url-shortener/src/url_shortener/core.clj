(ns url-shortener.core
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]))


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


(def db
  (io/as-file "url.txt"))


(def host "http://otus-url/")


(defn shorten-url [url]
  (spit db url :append true)
  (spit db \newline :append true)

  (with-open [file (io/reader db)]
    (let [url-id (count (line-seq file))
          hash   (id->url url-id)]
      (println "Your short URL:" (str host hash)))))


(defn find-long-url [url]
  (let [hash        (subs url (count host))
        line-number (url->id hash)]
    (with-open [file (io/reader db)]
      (let [original-url (nth (line-seq file) (dec line-number))]
        (println "Your original URL:" original-url)))))



(defn -main [command url]
  (when-not (.exists db)
    (.createNewFile db))

  (case command
    "shorten" (shorten-url url)
    "find" (find-long-url url)
    (println "Unknown command:" command)))



(comment

 (-main "shorten" "https://clojure.org/about/rationale")
 (-main "shorten" "https://otus.ru/lessons/clojure-developer/")

 (-main "find" "http://otus-url/b")
 (-main "find" "http://otus-url/c"))

;; lein run shorten "https://clojure.org/about/rationale"

;; lein uberjar
;; java -jar url-shortener.jar shorten "https://clojure.org/about/rationale"
