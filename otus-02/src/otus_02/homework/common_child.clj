(ns otus-02.homework.common-child)


;; Строка S называется потомком другой строки P,
;; если она (S) может быть образована путем удаления 0 или более символов из другой строки (P).
;; Буквы нельзя переставлять.
;; Имея две строки одинаковой длины, какую самую длинную строку можно построить так,
;; чтобы она была потомком обеих строк?

;; Например 'ABCD' и 'ABDC'

;; Эти строки имеют два дочерних элемента с максимальной длиной 3, ABC и ABD.
;; Их можно образовать, исключив D или C из обеих строк.
;; Ответ в данном случае - 3

;; Еще пример HARRY и SALLY. Ответ будет - 2, так как общий элемент у них AY


;; Хелпер, который строит следующую строку dynamic programming таблицы по текущей
(defn next-row-helper [x y-seq row]
  (loop [y-seq    y-seq
         row      row
         next-row '(0)]
    (if (empty? y-seq)
      next-row
      (let [y        (first y-seq)
            top-left (first row)
            top      (first (rest row))
            left     (first next-row)]
        (if (= x y)
          (recur
            (rest y-seq)
            (rest row)
            (conj next-row (inc top-left)))
          (recur
            (rest y-seq)
            (rest row)
            (conj next-row (max top left))))))))

;; По сути это задача на нахождение LCS (longest common subsequence)
;; Решение - dynamic programming, но без хранения всей таблицы в памяти, а только 2х строк: предыдущей и текущей
;; Ячейка на последней строке и последней строчке и будет является LCS
(defn common-child-length [x y]
  (first
    (let [x-seq (seq x)
          y-seq (seq y)]
      (loop [
             x-seq x-seq
             row   (repeat (inc (count y-seq)) 0)]
        (if (empty? x-seq)
          row
          (recur
            (rest x-seq)
            (next-row-helper (first x-seq) y-seq (reverse row))))))))