(ns otus-09.core
  (:require [clojure.core.match :refer [match]])
  (:gen-class))


(defn rect [width height]
  {:shape :rect :width width :height height})

(defn circle [radius]
  {:shape :circle :radius radius})

(def r (rect 4 13))

(def c (circle 12))

;; case 

(defn area [object]
  (case (:shape object)
    :rect (* (:width object) (:height object))
    :circle (* Math/PI (:radius object) (:radius object))
    (throw (ex-info "Unknown shape" {:shape object}))))

(area r)

(area c)

(area {})

;; cond + instance? 

(defn append
  "Adds an element x to the end of any sequential collection, faster for
  vectors."
  [coll x]
  (cond
    (instance? clojure.lang.PersistentVector coll)
    (conj coll x)

    (instance? clojure.lang.IPersistentList coll)
    (concat coll (list x))

    :else (str "Sorry, I don't know how to append to a " (type coll))))

(append [1 2 3] 1)

(append '(1 2 3) 1)

(append {} 1)

;; clojure.core.match 

(doseq [n (range 1 17)]
  (println
   (match [(mod n 3) (mod n 5)]
          [0 0] "FizzBuzz"
          [0 _] "Fizz"
          [_ 0] "Buzz"
          :else n)))

(defn area- [object]
  (match [object]
         [{:shape :rect :width w :height h}] (* w h)
         [{:shape :circle :radius r}] (* Math/PI r r)
         :else (throw (ex-info "Unknown shape" {:shape object}))))

(area- r)

(area- c)

(area- {})

;; multimethods  

(defmulti area* :shape)

(defmethod area* :rect [r]
  (* (:width r) (:height r)))

(defmethod area* :circle [c]
  (* Math/PI (:radius c) (:radius c)))

(defmethod area* :default [s]
  (throw (ex-info "Unknown shape" {:shape s})))

(area* r)

(area* c)

(area* {})

;; 

(defmulti factorial identity)

(defmethod factorial 0 [_]  1)

(defmethod factorial :default [num]
  (* num (factorial (dec num))))

;; 

(def quick-sort-threshold 5)
(defmulti my-sort (fn [arr]
                    (if (every? integer? arr)
                      :counting-sort
                      (if (< (count arr) quick-sort-threshold)
                        :quick-sort
                        :merge-sort))))

(defmethod my-sort :counting-sort [arr]
  "Counting for the win!")

(defmethod my-sort :quick-sort [arr]
  "Quick Sort it is")

(defmethod my-sort :merge-sort [arr]
  "Good ol' Merge Sort")

(my-sort [1 2 3])

(my-sort [1 2 3 "a"])

(my-sort [1 2 3 4 "a"])

;; multimethods-2 

(defmulti encounter (fn [x y] [(:species x) (:species y)]))

(defmethod encounter [:bunny :lion]
  [_ _]
  :run-away)

(defmethod encounter [:lion :bunny]
  [_ _]
  :eat)

(defmethod encounter [:lion :lion]
  [_ _]
  :fight)

(defmethod encounter [:bunny :bunny]
  [_ _]
  :mate)

(def b1 {:species :bunny :other :stuff})
(def b2 {:species :bunny :other :stuff})
(def l1 {:species :lion :other :stuff})
(def l2 {:species :lion :other :stuff})

(encounter b1 b2)

(encounter b1 l1)

(encounter l1 b1)

(encounter l1 l2)

(map encounter
     [b1 b1 l1 l1]
     [b2 l1 b1 l2])

;; ad-hoc иерархии 

(defmulti describe class)

(defmethod describe ::collection [c]
  (format "%s is a collection" c))

(defmethod describe String [s]
  (format "%s is a string" s))

(isa? :radius :radius)

(isa? java.util.Map ::collection)
(isa? java.util.Collection ::collection)

;;(derive java.util.Map ::collection)
;;(derive java.util.Collection ::collection)

(describe [])

(describe (java.util.HashMap.))

(describe "bar")

(parents java.util.AbstractMap)
(ancestors java.util.AbstractMap)
(descendants ::collection)

;; prefer-method 

(defmulti os-kind :os)

(defmethod os-kind ::unix
  [_]
  "UNIX")

(derive ::macos ::unix)

(os-kind {:os ::macos})

(defmethod os-kind ::bsd
  [_]
  "BSD")

(derive ::macos ::bsd)

(parents ::macos)
(ancestors ::macos)

;;(prefer-method os-kind ::unix ::bsd)

(os-kind {:os ::macos})
