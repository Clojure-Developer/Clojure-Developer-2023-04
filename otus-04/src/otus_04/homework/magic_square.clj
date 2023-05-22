(ns otus-04.homework.magic-square)

(defn make-board [n]
  (vec (repeat n (vec (repeat n 0)))))

(defn cell [board x y]
  (get-in board [y x] 0))

(defn filled? [board x y] (pos? (cell board x y)))

(defn update-cell [board x y v]
  (assoc-in board [y x] v))

(defn up-right-move [board x y]
  (let [n (count board)
        x' (mod (inc x) n)
        y' (mod (dec y) n)]
    [x' y']))

(defn down-move [board x y]
  (let [n (count board)
        y' (mod (inc y) n)]
    [x y']))

(defn next-move [board x y]
  (let [[x' y'] (up-right-move board x y)]
    (if (filled? board x' y')
      (down-move board x y)
      [x' y'])))

;; Оригинальная задача:
;; https://www.codewars.com/kata/570b69d96731d4cf9c001597
;;
;; Подсказка: используйте "Siamese method"
;; https://en.wikipedia.org/wiki/Siamese_method

(defn magic-square
  "Функция возвращает вектор векторов целых чисел,
  описывающий магический квадрат размера n*n,
  где n - нечётное натуральное число.

  Магический квадрат должен быть заполнен так, что суммы всех вертикалей,
  горизонталей и диагоналей длиной в n должны быть одинаковы."
  [n]
  (let [max-value (* n n)]
    (loop [board (make-board n)
           [x y] [(int (/ n 2)) 0]
           value 1]
      (if (> value max-value)
        board
        (let [new-board (update-cell board x y value)]
          (recur
            new-board
            (next-move new-board x y)
            (inc value)))))))
