(ns otus-04.homework.magic-square)

;; Оригинальная задача:
;; https://www.codewars.com/kata/570b69d96731d4cf9c001597
;;
;; Подсказка: используйте "Siamese method"
;; https://en.wikipedia.org/wiki/Siamese_method

(defn set-value [square [x y] value]
  (assoc-in square [y x] value))

(defn get-value [square [x y]]
  (get-in square [y x]))

(defn next-position
  [square [prev-x prev-y]]
  (let [n (count square)
        x (inc prev-x) ;; column
        y (dec prev-y)] ;; row
    (if (and (= x n) (= y -1))
      [(- n 2) 0]
      (let [x (mod x n)
            y (if (< y 0)
                 (dec n)
                 y)]
        (if (not= (get-value square [x y]) 0)
          [(- x 2) (inc y)]
          [x y])))))

(defn fill
  ([square] (let [n        (count square)
                  position [(dec n) (int (/ n 2))]
                  square   (set-value square position 1)]
              (fill square position 2)))
  ([square prev-position prev-value]
   (letfn [(fill-inner [square position value]
             (let [n (count square)
                   max-value (* n n)
                   position (next-position square position)]
               (if (= value (inc max-value))
                 square
                 (recur (set-value square position value) position (inc value)))))]
     (fill-inner square prev-position prev-value))))


(defn magic-square
  "Функция возвращает вектор векторов целых чисел,
  описывающий магический квадрат размера n*n,
  где n - нечётное натуральное число.

  Магический квадрат должен быть заполнен так, что суммы всех вертикалей,
  горизонталей и диагоналей длиной в n должны быть одинаковы."
  [n]
  {:pre [(odd? n)]}
  (let [square (vec (repeat n (vec (repeat n 0))))]
    (fill square)))
