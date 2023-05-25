(ns otus-07.core
  (:require [clojure.string :as str]))

;; 
;; Регулярные выражения

(type #"pattern \d")
(type (re-pattern "pattern \\d"))

(re-matches #"hello" "hello, world")

(re-matches #"hello.*" "hello, world")

(re-matches #"(?i)hello.*" "Hello, World")

(re-matches #"hello, (.*)" "hello, world")

(let [full-name "Rich Hickey"]
  (if-some [[whole-match first-name last-name]
            (re-matches #"(\w+)\s(\w+)" full-name)]
    (println last-name first-name)
    (println "Unparsable name")))

(re-matches #"\p{L}+\s\p{L}+" "Рич Хикки")

(re-find #"sss" "Loch Ness")

(re-find #"s+" "dress")

(re-find #"s+(.*)(s+)" "success")

(re-find #"[a-zA-Z](\d+)" "abc x123 b44 234")

(re-seq #"[a-zA-Z](\d+)" "abc x123 b44 234")

(let [patt #"(?<area>\d{3})-(?<prefix>\d{3})-(?<tail>\d{4})"
      matcher (re-matcher patt "619-239-5464")]
  (println (re-find matcher))
  (println (type matcher))
  (println (re-groups matcher))
  (println (.group matcher "area")))

(str/replace "mississippi" #"i.." "obb")

(str/replace "mississippi" #"(i)" "$1$1")

(str/replace "mississippi" #"(.)i(.)"
             (fn [[_ b a]]
               (str (str/upper-case b)
                    "-"
                    (str/upper-case a))))

(str/split "q12w3e4r5t6y7u8i9o0p" #"\d+" 5)


;; 
;; Иммутабельные очереди (FIFO)

(defmethod print-method clojure.lang.PersistentQueue [queue writer]
  (print-method '<- writer)
  (print-method (seq queue) writer)
  (print-method '-< writer))

(defn queue
  ([] (clojure.lang.PersistentQueue/EMPTY))
  ([coll]
   (reduce conj clojure.lang.PersistentQueue/EMPTY coll)))

(def numbers-in (conj (queue) 1 2 3 4 5))

(peek numbers-in)

(pop numbers-in)

numbers-in

(empty? numbers-in)
(empty? (queue))

;; 
;; List comprehension

(defn cartesian-product [a b]
  (for [x a, y b]
    [x y]))

(cartesian-product [:a :b :c] [1 2 3])

(for [i (range 3)]
  (for [j (range 3)]
    [i j]))

(for [i (range 3)
      j (range 3)]
  [i j])

(for [x (range 5)
      :let [y (* x 3)]
      :when (even? y)]
  y)

(for [a (range 2)
      b (range 2)
      :while (<= a b)
      c (range 2)]
  [a b c])

(for [a (range 2)
      b (range 2)
      c (range 2)
      :while (<= a b)]
  [a b c])

;; Given an array of integers, return indices of the two numbers such that they
;; add up to a specific target.
(defn two-sum [nums target]
  (let [nums-index (zipmap nums (range))
        indexs (for [[x i] nums-index
                     [y j] nums-index
                     :when (<= i j)
                     :when (= (+ x y) target)]
                 [i j])]
    (first indexs)))

(two-sum [2 7 11 15] 9)

;; 
;; Функции семейства do

(doseq [x (range 5)
        :let [y (* x 3)]
        :when (even? y)]
  (println y))

(let [dummy (map println (range 5))]
  ;; (dorun dummy)
  ;; (doall dummy)
  )

(dorun 5 (repeatedly #(println "hi")))
;; (doall 5 (repeatedly #(println "hi"))) ;; TODO не надо так :')

(dotimes [n 5]
  (println "n is" n))

(run! println (range 5))
