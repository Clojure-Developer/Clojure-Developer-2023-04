(ns spec-sampler.generate
  (:require [clojure.spec.alpha :as s]
            [cheshire.core :as chs]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as str]))

(defn gen-string
  ([len]
   (-> (gen/char-alpha)
       (gen/sample len)
       (#(apply str %))
       str/lower-case))
  ([]
   (gen-string (+ 5 (rand-int 10)))))
  

(defmulti generate :type)

(defmethod generate "string"
  [{:keys [format]}]
  (case format
        "phone" (apply str (gen/sample (gen/choose 0 9) 10))
        "url" (str "https://"
                   (gen-string) "."
                   (gen-string 3))
        "email" (str (gen-string) "@"
                     (gen-string) "."
                     (gen-string 3))    
        (gen-string)))

(defmethod generate "integer"
  [{:keys [format]}]
  (case format
    "pos" (gen/generate (s/gen (s/and int? pos?)))
    (gen/generate (gen/int))))

(defmethod generate "object"
  [{:keys [properties]}]
  (reduce-kv #(assoc %1 %2 (generate %3)) {} properties))

(defmethod generate "number"
  [{:keys [format]}]
  (case format
    "integer" (generate {:type "integer"})
    (gen/generate (gen/double))))
  

(defmethod generate :default
 [args]
 args)
 
(defn genrate-sample [spec]
  (-> spec
      (chs/parse-string true)
      generate
      (chs/generate-string)))
