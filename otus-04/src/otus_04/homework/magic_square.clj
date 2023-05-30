(ns otus-04.homework.magic-square)

;; Оригинальная задача:
;; https://www.codewars.com/kata/570b69d96731d4cf9c001597
;;
;; Подсказка: используйте "Siamese method"
;; https://en.wikipedia.org/wiki/Siamese_method


(defn find-position-square
  "Найти позицию числа в магическом квадрате.
   
   Args:
   * sq - магический квадрат
   * n  - натуральное число
   * i  - текущий инкремент"
  [sq n i]
  (let [idx (.indexOf (flatten sq) i)
        x   (int (/ idx n))
        y   (mod idx n)]
    [x y]))


(defn init-square
  "Инициализируем 0-ми магический квадрат.
   
   Args:
   * n - натуральное число"
  [n]
  (vec (repeat n (vec (repeat n 0)))))


(defn gen-square
  "Заполняем магический квадрат числами согласно правилам.
   
   Args:
   * sq - магический квадрат
   * n  - натуральное число
   * x  - позиция строки
   * y  - позиция колонки
   * i  - текущий инкремент"
  [sq n x y i]
  (cond
    ;; Выходим из рекурсии, когда заполнили весь квадрат.
    (> i (* n n))
    sq

    ;; Если вышли за верхний предел, то переходим к нижней строке.
    (< x 0)
    (let [x (dec n)]
      (gen-square sq n x y i))

    ;; Если вышли за правый предел, то переходим к левой колонке.
    (= y n)
    (let [y 0]
      (gen-square sq n x y i))

    ;; Если ячейка занята, то переходим к пред. числу.
    (not= (get-in sq [x y]) 0)
    (let [[x* y] (find-position-square sq n (dec i))
          x (inc x*)]
      (gen-square sq n x y i))

    :else
    (let [sq (assoc-in sq [x y] i)
          x (dec x)
          y (inc y)
          i (inc i)]
      (gen-square sq n x y i))))


(defn magic-square
  "Функция возвращает вектор векторов целых чисел,
  описывающий магический квадрат размера n*n,
  где n - нечётное натуральное число.

  Магический квадрат должен быть заполнен так, что суммы всех вертикалей,
  горизонталей и диагоналей длиной в n должны быть одинаковы."
  [n]
  ;; Задаем начальные параметры
  (let [sq (init-square n)
        x  0
        y  (int (/ n 2))
        i  1]
    (gen-square sq n x y i)))

