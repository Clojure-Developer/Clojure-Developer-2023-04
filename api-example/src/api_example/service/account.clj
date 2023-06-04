(ns api-example.service.account
  (:require [api-example.dao.account :as dao.account]
            [api-example.util :as u]
            [ring.util.response :as response]))

(defn get-all
  [{:keys [ds]}]
  (response/response (dao.account/get-all ds)))

(defn get-by-id
  [{:keys [ds]} id]
  (if-let [account (dao.account/get-by-id ds id)]
    (response/response account)
    (response/not-found {:message (str "User with id " id " is not found.")})))

(defn create
  [{:keys [ds]} entity]
  (let [entity-d1 (assoc entity :created-on (u/now))
        entity-d2 (dao.account/create ds entity-d1)]
    (response/response entity-d2)))
