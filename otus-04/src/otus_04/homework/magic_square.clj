(ns otus-04.homework.magic-square
  (:require [clojure.math :refer [floor-div]]))

;; Оригинальная задача:
;; https://www.codewars.com/kata/570b69d96731d4cf9c001597
;;
;; Подсказка: используйте "Siamese method"
;; https://en.wikipedia.org/wiki/Siamese_method

(defn- fill [n]
  (first
   (reduce
    (fn [[field [x y]] i]
      [(assoc field [x y] i)
       (let [p [(mod (inc x) n)
                (mod (dec y) n)]]
         (if (field p)
           [x (mod (inc y) n)]
           p))])
    [{}
     [(floor-div n 2) 0]]
    (range 1 (inc (* n n))))))

(defn magic-square
  "Функция возвращает вектор векторов целых чисел,
  описывающий магический квадрат размера n*n,
  где n - нечётное натуральное число.

  Магический квадрат должен быть заполнен так, что суммы всех вертикалей,
  горизонталей и диагоналей длиной в n должны быть одинаковы."
  [n]
  (let [field (fill n)]
    (vec (for [y (range n)]
           (vec (for [x (range n)]
                  (field [x y])))))))
