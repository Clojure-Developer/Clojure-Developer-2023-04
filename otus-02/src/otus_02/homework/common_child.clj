(ns otus-02.homework.common-child
  (:require [portal.api :as p]
            [sc.api]))


(comment
 (def p (p/open {:launcher :intellij}))
 (add-tap #'p/submit)
 (tap> :hello)
 (tap> :world)

 @p



 (tap> {:asd "qweqw"})


 (defn my-tap [x]
   (tap> x)
   x)

 (sc.api/spy)
 (sc.api/defsc 7)
 (sc.api/letsc 7))



;; Строка называется потомком другой строки,
;; если она может быть образована путем удаления 0 или более символов из другой строки.
;; Буквы нельзя переставлять.
;; Имея две строки одинаковой длины, какую самую длинную строку можно построить так,
;; чтобы она была потомком обеих строк?

;; Например 'ABCD' и 'ABDC'

;; Эти строки имеют два дочерних элемента с максимальной длиной 3, ABC и ABD.
;; Их можно образовать, исключив D или C из обеих строк.
;; Ответ в данном случае - 3

;; Еще пример HARRY и SALLY. Ответ будет - 2, так как общий элемент у них AY


(defn longest [xs ys]
  (if (> (count xs) (count ys))
    xs
    ys))


(def lcs
  (memoize
    (fn [[x & xs] [y & ys]]
      (cond
        (or (= x nil) (= y nil)) nil
        (= x y) (cons x (lcs xs ys))
        :else (longest (lcs (cons x xs) ys)
                       (lcs xs (cons y ys)))))))


(comment
 (count (lcs "SHINCHAN" "NOHARAAA")))


(defn lcs-table [^String x ^String y]
  (let [table (apply conj [(vec (repeat (inc (count x)) ""))]
                     (repeat (count y) (vec (repeat (inc (count x)) ""))))]
    (if (or (empty? x) (empty? y))
      ""
      (loop [row-ix 1 col-ix 1 t table]
        (if (> row-ix (count y))
          (get-in t [(count y) (count x)]) ; outside of table size limits - calculation finished
          (recur (if (= col-ix (count x)) (inc row-ix) row-ix)
                 (if (= col-ix (count x)) 1 (inc col-ix))
                 (assoc-in t [row-ix col-ix]
                           (if (= (get y (dec row-ix)) (get x (dec col-ix)))
                             (str (get-in t [(dec row-ix) (dec col-ix)]) (get y (dec row-ix)))
                             (if (>= (count (get-in t [(dec row-ix) col-ix]))
                                     (count (get-in t [row-ix (dec col-ix)])))
                               (get-in t [(dec row-ix) col-ix])
                               (get-in t [row-ix (dec col-ix)]))))))))))


(defn common-child-length [first-string second-string]
  (count (lcs-table first-string second-string)))
