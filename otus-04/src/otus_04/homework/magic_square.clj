(ns otus-04.homework.magic-square)

;; Оригинальная задача:
;; https://www.codewars.com/kata/570b69d96731d4cf9c001597
;;
;; Подсказка: используйте "Siamese method"
;; https://en.wikipedia.org/wiki/Siamese_method

(defn next-pos 
  "Возвращает следующую позицию в квадрате для обхода по сиамскому методу."
  [square n [x y]]
  (let [pos [(rem (inc x) n) (rem (inc y) n)]]
    (if (zero? (get-in square pos)) pos
      [x (rem (+ n y -1) n)])))

(defn magic-square
  "Функция возвращает вектор векторов целых чисел,
  описывающий магический квадрат размера n*n,
  где n - нечётное натуральное число.

  Магический квадрат должен быть заполнен так, что суммы всех вертикалей,
  горизонталей и диагоналей длиной в n должны быть одинаковы."
  [n]
  (loop [square (mapv vec (repeat n (repeat n 0)))
         i (* n n)
         pos [(quot n 2) (dec n)]]
    (if (zero? i) square
      (recur (assoc-in square pos i)
             (dec i)
             (next-pos square n pos)))))
