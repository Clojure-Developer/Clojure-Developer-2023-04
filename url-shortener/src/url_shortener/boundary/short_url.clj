(ns url-shortener.boundary.short-url
  (:require
   [duct.database.sql]
   [honey.sql :as honey]
   [next.jdbc.sql :as sql]
   [next.jdbc.result-set :refer [as-unqualified-lower-maps]]))


(defprotocol URLs
  (list-all   [this])
  (find-by-id [this id])
  (save       [this key url]))


(def ^:private all-urls-query
  (honey/format
   {:select [[:id :hash] :url]
    :from :urls}))


(extend-protocol URLs
  duct.database.sql.Boundary
  
  (list-all [{db :spec}]
    (sql/query db all-urls-query {:builder-fn as-unqualified-lower-maps}))
  
  (find-by-id [{db :spec} id]
    (-> (sql/get-by-id db :urls id)
        :URLS/URL))
  
  (save [{db :spec} key url]
    (sql/insert! db :urls {:id key :url url})))
