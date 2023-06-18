(ns otus-12.ini
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(defn ->conformer
  ([f]
   (->conformer f identity))
  ([f unf]
   (s/conformer (fn [value]
                  (try
                    (f value)
                    (catch Exception _
                      ::s/invalid)))
                unf)))
(s/def ::->int
  (->conformer (fn [value]
                   (Long/valueOf value))))

(s/def ::int
  (s/or :int int?
        :string ::->int))

(s/def ::->boolean
  (s/and (->conformer str/lower-case)
         (->conformer (fn [value]
                          (case value
                            ("true" "1" "on" "yes") true
                            ("false" "0" "off" "no") false
                            ::s/invalid)))))

(s/def ::boolean
  (s/or :boolean boolean?
        :string ::->boolean))

(s/def ::value
  (s/or
    :int ::int
    :boolean ::boolean
    :string string?))

(s/def ::->keyword (->conformer keyword))

(s/def ::->kv-pair
  (s/tuple ::->keyword ::value))

(s/def ::str->kv-pair
  (s/and
    (->conformer #(str/split % #"="))
    ::->kv-pair))

(s/def ::str->section-name
  (s/and
    (->conformer
      #(if-some [r (second (re-find #"\[(.*)\]" %))] r ::s/invalid))
    ::->keyword))

(s/def ::section
  (s/* ::str->kv-pair))

(s/def ::section-with-name
  (s/cat :name ::str->section-name
         :section ::section))

(s/def ::ini
  (s/* ::section-with-name))

(s/def ::str->ini
  (s/and
    (->conformer #(filter not-empty (str/split-lines %)))
    ::ini))

(defn parse-ini
  [input]
  (let [ast (s/conform ::str->ini input)
        params-mapper (map (fn [[key value]]
                             [key (s/unform ::value value)]))
        section-mapper (map (fn [{:keys [name section]}]
                              [name (into {} params-mapper section)]))]
    (into {} section-mapper ast)))

(def ini-example "[database]\nhost=localhost\nport=5432\nuser=test\nencrypt=true\n\n[server]\nhost=127.0.0.1\nport=8080")

(parse-ini ini-example)
