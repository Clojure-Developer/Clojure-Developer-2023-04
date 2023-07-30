(ns api-example.spec
  (:require [clojure.spec.alpha :as s]))

(s/def ::dbtype string?)
(s/def ::dbname string?)
(s/def ::host string?)
(s/def ::port nat-int?)
(s/def ::user string?)
(s/def ::password string?)

(s/def ::config
  (s/keys :req-un [::dbtype
                   ::dbname
                   ::host
                   ::port
                   ::user
                   ::password]))
