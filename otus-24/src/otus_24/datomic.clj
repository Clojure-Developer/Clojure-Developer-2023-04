(ns otus-24.datomic
  (:require [datomic.api :as d]))

(def db-uri "datomic:mem://test")
(d/create-database db-uri)
(def conn (d/connect db-uri))

(def schema-tx
  [{:db/ident :mage/name
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/string
    :db/unique :db.unique/identity
    :db/doc "A mage's name"}
   {:db/ident :mage/age
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/long}
   {:db/ident :mage/spells
    :db/valueType :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/doc "Spellbook, cons"}
   {:db/ident :spell/name
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/string
    :db/unique :db.unique/identity}
   {:db/ident :spell/manacost
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/long}])

(d/transact conn schema-tx)

(d/pull (d/db conn) '[*] :mage/name)
