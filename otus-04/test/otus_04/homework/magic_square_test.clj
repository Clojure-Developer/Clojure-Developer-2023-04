(ns otus-04.homework.magic-square-test
  (:require
   [clojure.test :refer :all]
   [otus-04.homework.magic-square :refer [magic-square]]))

(defn magic? [square]
  (let [size (count square)
        rows (map (partial reduce +) square)
        cols (for [i (range size)]
               (reduce + (map #(nth % i) square)))
        diag1 (reduce
               + (for [i (range size)]
                   (get-in square [i i])))
        diag2 (reduce
               + (for [i (range size)]
                   (get-in square [(- size i 1) i])))]
    (= 1 (count (set (concat rows cols [diag1 diag2]))))))

(deftest- magic?-test
  (is (not (magic? [[1 2 3]
                    [4 5 6]
                    [7 8 9]])))
  (is (magic? [[8 1 6]
               [3 5 7]
               [4 9 2]]))
  (is (magic? [[17 24  1  8 15]
               [23  5  7 14 16]
               [ 4  6 13 20 22]
               [10 12 19 21  3]
               [11 18 25  2  9]])))

(defn square-of-size? [n sq]
  (and (= n (count sq))
       (every? (partial = n)
               (map count sq))))

(deftest magic-square-test
  (let [sq (magic-square 1)]
    (is (magic? sq))
    (is (square-of-size? 1 sq)))

  (let [sq (magic-square 3)]
    (is (magic? sq))
    (is (square-of-size? 3 sq)))

  (let [sq (magic-square 5)]
    (is (magic? sq))
    (is (square-of-size? 5 sq))))
