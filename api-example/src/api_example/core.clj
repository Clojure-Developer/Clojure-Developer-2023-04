(ns api-example.core
  (:require [camel-snake-kebab.core :as csk]
            [clojure.spec.alpha :as s]
            [integrant.core :as ig]
            [api-example.middleware]
            [api-example.router :refer [router]]
            [api-example.spec :as spec]
            [api-example.util :as u]
            [migratus.core :as migratus]
            [next.jdbc :as jdbc]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json])
  (:gen-class))

;; DB

(defmethod ig/pre-init-spec :core/db [_]
  (s/keys :req-un [::spec/config]))

(defmethod ig/init-key :core/db
  [_ {:keys [config]}]
  config)

;; Migrator

(defmethod ig/init-key :core/migrator
  [_ {:keys [db]}]
  (let [ds (jdbc/get-datasource db)
        config {:store :database
                :migration-dir "migrations"
                :db {:datasource ds}}]
    (migratus/migrate config)
    ds))

(comment
  (migratus/create {:migration-dir "migrations"} "create-account-role")
  (migratus/create {:migration-dir "migrations"} "create-account" :edn)
  )

;; App

(def ^:dynamic *ctx*
  nil)

(defmethod ig/init-key :core/app
  [_ {:keys [ds]}]
  (binding [*ctx* {:ds ds}]
    (-> router
        
        (ring.middleware.json/wrap-json-body {:key-fn csk/->kebab-case-keyword})
        
        (api-example.middleware/wrap-request-ctx *ctx*)

        (api-example.middleware/wrap-postgres-exception)
        (api-example.middleware/wrap-fallback-exception)
        
        (ring.middleware.json/wrap-json-response {:key-fn csk/->camelCaseString}))))

;; Server

(defmethod ig/init-key :core/server
  [_ {:keys [app config]}]
  (run-jetty app config))

(defmethod ig/halt-key! :core/server
  [_ server]
  (.stop server))

;; Whole system

(defonce system
  (atom nil))

(defn start-system
  [config]
  (when @system
    (ig/halt! @system))
  (reset! system (ig/init config)))

(defn -main
  [profile & _args]
  (let [profile-kw (keyword profile)
        config (u/load-config "config.edn" {:profile profile-kw})]
    (println config)
    (start-system config)))

#_(-main "local")
#_(ig/halt! @system)
