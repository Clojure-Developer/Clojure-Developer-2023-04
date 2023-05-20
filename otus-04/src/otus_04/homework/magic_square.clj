(ns otus-04.homework.magic-square)

(defn make-board [n]
  (vec (repeat n (vec (repeat n 0)))))

(defn coalesce [& args]
  (first (filter (complement nil?) args)))

(defn cell [board x y]
  (coalesce (get-in board [y x]) 0))

(defn filled? [board x y] (> (cell board x y) 0))

(defn update-cell [board x y v]
  (update-in board [y x] (constantly v)))

(defn up-right-move [board x y]
  (let [n (count board)
        x' (rem (+ x  1 n) n)
        y' (rem (+ y -1 n) n)]
    [x' y']))

(defn down-move [board x y]
  (let [n (count board)
        y' (rem (+ y 1 n) n)]
    [x y']))

(defn next-move [board x y]
  (let [[x' y'] (up-right-move board x y)]
    (if (not (filled? board x' y'))
      [x' y']
      (down-move board x y))))

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
  (loop [board (make-board n)
         [x y] [(int (/ n 2)) 0]
         value 1]
    (if (> value (* n n))
      board
      (let [new-board (update-cell board x y value)]
        (recur
          new-board
          (next-move new-board x y)
          (inc value))))))