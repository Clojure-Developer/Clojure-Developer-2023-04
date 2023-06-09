(ns otus-11.core)

;; * Sequence abstraction

(first [1 2 3])
(first '(1 2 3))
(first {:a 1 :b 2})

(next [])
(rest (rest []))

;; * Ленивость

(defn geom [start mul stop]
  (lazy-seq
   (when (< start stop)
     (println (str "=> " start))
     (cons start
           (geom (* start mul) mul stop)))))

(lazy-cat
 [1 2]
 '(3 4))

;; * Бесконечные последовательности

(defn geom-inf [start mul]
  (lazy-seq
   (cons start
         (geom-inf (* start mul) mul))))

(def three-nums
  (lazy-cat
   '(1 2)
   (do (println "calculating third num")
       [3])))

(comment
  (def chunks []
    (lazy-cat
     (lazy-seq [1 2 3])
     (chunks))))

;; * Трансдьюсеры
;; ** reducers

(defn conj-even
  ([] [])
  ([acc v]
   (if (even? v)
     (conj acc v)
     acc)))

;; ** transduce

(transduce (filter even?) conj (range 10))

(defn conj-tr
  ([]
   (println "(conj-tr)")
   [])
  ([acc]
   (println (str "(conj-tr " acc ")"))
   acc)
  ([acc v]
   (println (str "(conj-tr " acc " " v ")"))
   (conj acc v)))

(defn avg
  ([] [0 0])
  ([[s c]] (float (/ s c)))
  ([[s c] v] [(+ s v) (inc c)]))

(transduce identity avg (range 10))

(defn xmap [f]
  (fn [reducer]
    (fn
      ([]
       (reducer))
      ([acc]
       (reducer acc))
      ([acc v]
       (reducer acc (f v))))))

(defn xfilter [f]
  (fn [reducer]
    (fn
      ([] (reducer))
      ([acc] (reducer acc))
      ([acc v]
       (if (f v)
         (reducer acc v)
         (reducer acc))
       ))))

(transduce
 (comp (xfilter odd?)
       (xmap str))
 conj
 (range 10))

;; ** отладка

(defn trace [prefix]
  (fn [reducer]
    (fn [& args]
      (println (str prefix ": " args))
      (apply reducer args))))

(transduce
 (comp (filter odd?)
       (map str)
       (trace ">"))
 conj
 (range 10))

(transduce
 (comp (filter odd?)
       (map str))
 ((trace ">") conj)
 (range 10))

;; ** into

(into '() (map inc) (range 10))

;; ** sequence

(def convejor (comp (filter ..)
                    (map ...
                         )
                    (filter ...)))

(sequence convejor (range 10))
