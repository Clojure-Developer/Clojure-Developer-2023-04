(ns url-shortener.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :refer [as-unqualified-lower-maps]]
   [next.jdbc.sql :as sql]
   [honey.sql :as honey]))


(def symbols
  "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")


(def host
  "http://localhost:8000/")


(def db (jdbc/get-datasource {:dbname "database"
                              :dbtype "h2"}))

(def ^:private table-sql
  (honey/format {:create-table [:urls :if-not-exists]
                 :with-columns
                 [[:id [:varchar 6] :primary-key]
                  [:url [:varchar 4096] :not-null]]}))

(defn init-storage []
  (jdbc/with-transaction [tx db]
    (jdbc/execute-one! tx table-sql)))


;; =============================================================================
;; URL -> Key
;; =============================================================================


(defn ^:private get-idx [i]
  (Math/floor (/ i 62)))


(defn ^:private get-symbol-by-idx [i]
  (get symbols (rem i 62)))


(defn ^:private int->key [id]
  (let [idx-sequence  (iterate get-idx id)
        valid-idxs    (take-while #(> % 0) idx-sequence)
        code-sequence (map get-symbol-by-idx valid-idxs)]
    (string/join (reverse code-sequence))))


(defn url->key [url]
  (int->key (java.lang.Integer/toUnsignedLong (hash url))))


;; =============================================================================
;; Application API
;; =============================================================================


(defn shorten-url [url]
  (let [key (url->key url)]
    (sql/insert! db :urls {:id key :url url})
    (str host key)))


(defn find-long-url [id]
  (:URLS/URL (sql/get-by-id db :urls id)))


(def ^:private query
  (honey/format
   {:select [[:id :hash] :url]
    :from :urls}))


(defn get-all-urls []
  (sql/query db query
             {:builder-fn as-unqualified-lower-maps}))
