(ns api-example.dao.util
  (:require [honey.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.date-time]
            [next.jdbc.result-set :as rs]))

(extend-protocol rs/ReadableColumn
  java.sql.Timestamp
  (read-column-by-label [^java.sql.Timestamp v _]
    (java.util.Date/from (.toInstant v)))
  (read-column-by-index [^java.sql.Timestamp v _ _]
    (java.util.Date/from (.toInstant v))))

(def format-opts
  {:dialect :ansi
   :quoted-snake true
   :quoted true})

(defn sql-format
  [data]
  (sql/format data format-opts))

(def execute-opts
  {:return-keys true
   :builder-fn rs/as-unqualified-kebab-maps})

(defn execute!
  [ds query]
  (jdbc/execute! ds query execute-opts))

(defn execute-one!
  [ds query]
  (jdbc/execute-one! ds query execute-opts))
