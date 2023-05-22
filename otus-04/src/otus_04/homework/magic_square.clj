(ns otus-04.homework.magic-square)

;; Оригинальная задача:
;; https://www.codewars.com/kata/570b69d96731d4cf9c001597
;;
;; Подсказка: используйте "Siamese method"
;; https://en.wikipedia.org/wiki/Siamese_method

(defn create-field [n]
  (vec (repeat n (vec (repeat n 0)))))

(defn next-coord [coord step size]
  (let [new-coord (+ coord step)]
    (cond
      (neg? new-coord) (+ new-coord size)
      (>= new-coord size) (- new-coord size)
      :else new-coord)))
  

(defn magic-square
  "Функция возвращает вектор векторов целых чисел,
  описывающий магический квадрат размера n*n,
  где n - нечётное натуральное число.

  Магический квадрат должен быть заполнен так, что суммы всех вертикалей,
  горизонталей и диагоналей длиной в n должны быть одинаковы."
  [n]
  {:pre [(odd? n)]}
  (loop [field (create-field n)
         prg (take (* n n) (iterate inc 1))
         x (quot n 2)
         y 0]
    (cond
      (empty? prg) field
      (pos? (get-in field [y x]))(recur
                                  field 
                                  prg 
                                  (next-coord x -1 n) 
                                  (next-coord y 2 n))
      :else (recur
             (assoc-in field [y x] (first prg))
             (rest prg)
             (next-coord x 1 n)
             (next-coord y -1 n)))))
      
(comment
  
  (magic-square 5))
