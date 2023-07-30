(ns otus-12.util
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as st]))

(defn ->conformer
  [f]
  (s/conformer (fn [value]
                 (try
                   (f value)
                   (catch Exception _
                     ::s/invalid)))
               identity))

(defn parse-spec
  [spec x]
  (let [result (s/conform spec x)]
    (if (s/invalid? result)
      result
      (s/unform spec result))))

(defonce
  ^{:doc "A map of environment variables."}
  env
  (merge {}
         (System/getenv)
         (System/getProperties)))

(defn instrument
  "Instruments all function specs defined by `sym-or-syms`
   if the program is running in dev mode."
  [sym-or-syms]
  (when (= "true" (get env "dev"))
    (st/instrument sym-or-syms)))

(defmacro instrument-ns
  "Instruments all function specs defined in the current namespace
   if the program is running in dev mode."
  []
  (let [ns-sym (ns-name *ns*)
        syms (st/enumerate-namespace ns-sym)]
    (instrument syms)))
