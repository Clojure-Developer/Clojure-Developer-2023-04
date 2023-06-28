(ns spec-sampler.parse
  (:require [clojure.spec.alpha :as s]
            [cheshire.core :as chs])
  (:gen-class))

(defn check-type [type]
  (fn [m]
    (= type (:type m))))
  

(s/def :oas/type #{"string" "integer" "object" "number"})
(s/def :string/format #{"url", "email" "phone"})
(s/def :int/format #{"pos"})
(s/def :number/format #{"double" "integer"})


(s/def :oas/string
  (s/and (check-type "string")
         (s/keys :req-un [:oas/type]
                 :opt-un [:string/format])))

(s/def :oas/int
  (s/and (check-type "integer")
         (s/keys :req-un [:oas/type]
                 :opt-un [:int/format])))

(s/def :oas/number
  (s/and (check-type "number")
         (s/keys :req-un [:oas/type]
                 :opt-un [:number/format])))

(s/def :object/properties 
  (s/map-of keyword? ::oas))

(s/def :oas/object 
  (s/and (check-type "object")
         (s/keys :req-un [:oas/type
                          :object/properties])))

(s/def ::oas (s/or :int :oas/int
                   :string :oas/string
                   :number :oas/number
                   :object :oas/object))


(defn spec-valid? [spec]
  (try (->> (chs/parse-string spec true)
            (s/valid? ::oas))
       (catch com.fasterxml.jackson.core.JsonParseException _
         false)))
