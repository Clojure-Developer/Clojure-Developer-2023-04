;; * Clojure Developer, урок 21
(ns otus-21.core
  (:require [clojure.zip :as z]
            [clojure.walk :as w]))

;; * Древовидные структуры

(def bin-tree
  [3 [1 nil [2 nil nil]]
   [7 [5 [4 nil nil] [6 nil nil]]
    [8 nil nil]]])

(def dict
  [:c [:a [:b
           :t]
       :olor]])

;; * Графы

;;   b - c
;;  /   /
;; a   e   g
;;  \ /   /
;;   d - f

;; ** Матрица смежности

;;   a b c d e f g
;; a x 1 0 1 0 0 0
;; b 1 x 1 0 0 0 0
;; c 0 1 x 0 1 0 1
;; d       x     .
;; e         x   .
;; f           x .
;; g . . . . . . x

;; ** Списки смежности

;; a: b, d
;; b: a, c
;; c: b, e, g
;; d: a, e, f
;; e: c, d
;; f: d
;; g: c

;; *** пример

(def gr
  {:a [:b :d]
   :b [:a :c]
   :c [:b :e :g]
   :d [:a :e :f]
   :e [:c :d]
   :f [:d]
   :g [:c]})

(def gr2
  {:a [nil [:b :d]]
   :b [:a [:c]]
   ;; ...
   })

;; * clojure.walk

;; ** walk

(comment
  (w/walk (fn [x] (if (number? x) (str x) x))
          identity bin-tree)

  (letfn [(sum [t]
            (cond (coll? t)
                  (w/walk sum (partial reduce +) t)

                  (nil? t) 0

                  true t))]
    (sum bin-tree)))

;; ** prewalk/postwalk

(comment
  (w/prewalk str bin-tree)
  (w/postwalk
   (fn [x] (if (number? x) (str x) x))
   bin-tree))

;; * clojure.zip

(comment
  (-> bin-tree
      z/vector-zip
      z/down
      z/right
      z/down
      z/rightmost
      z/down
      (z/edit + 100)
      z/root)

  (-> bin-tree
      z/vector-zip
      z/down
      z/right
      z/down))

;; FIXME: broken
(comment
  (loop [c (z/vector-zip bin-tree)]
    (let [n (if (odd? (z/node c))
              (z/edit c + 100)
              c)]
      (if (z/branch? n)
        (recur (z/down n))
        (loop [n (z/up n)
               prev n]
          (let [[found n]
                (cond (nil? n)
                      [true (z/root prev)]

                      (nil? (z/right n))
                      (recur (z/up n) n)

                      [false (z/right n)])]
            (if found n
                (recur n))))))))

;; ** zipper

(def gr-zipper
  (z/zipper
   (constantly true)
   #(get gr %)
   (fn [n _] n)
   :a))

(comment
  (-> gr-zipper
      z/down
      z/down
      z/right
      z/down
      z/right
      z/right
      z/children))

;; * Поиск
