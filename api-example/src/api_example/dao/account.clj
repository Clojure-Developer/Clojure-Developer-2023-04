(ns api-example.dao.account
  (:require [api-example.dao.util :refer [sql-format execute! execute-one!]]))

(def table
  :account)

(defn get-all
  [ds]
  (let [query (sql-format {:select [:*]
                           :from table})]
    (execute! ds query)))

(defn get-by-id
  [ds id]
  (let [query (sql-format {:select [:*]
                           :from table
                           :where [:= :id id]})]
    (execute-one! ds query)))

(defn create
  [ds entity]
  (let [query (sql-format {:insert-into table
                           :values [entity]})]
    (execute-one! ds query)))
