(ns api-example.api.account
  (:require [compojure.core :as c]
            [compojure.coercions :refer [as-int]]
            [api-example.service.account :as service.account]))

(def routes
  (c/context "/accounts" {:keys [ctx]}
    (c/GET "/" [] (service.account/get-all ctx))
    (c/POST "/" {:keys [body]} (service.account/create ctx body))
    (c/context "/:id" [id :<< as-int]
      (c/GET "/" [] (service.account/get-by-id ctx id)))))
