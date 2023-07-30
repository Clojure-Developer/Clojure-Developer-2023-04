(ns api-example.router
  (:require [clojure.string :as string]
            [compojure.core :as c]
            [api-example.api.account :as api.account]))

(defn not-found
  [request]
  (let [{:keys [uri
                request-method]} request]
    {:status 404
     :body {:message (format "There is no handler for %s %s"
                             (string/upper-case (name request-method))
                             uri)}}))

(def router
  (c/routes
   (c/context "/" {:keys [_ctx]}
     api.account/routes)
   not-found))
