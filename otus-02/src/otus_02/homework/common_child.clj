(ns otus-02.homework.common-child)


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


(defn common-child-length [first-string second-string]
  (cond
    (or (empty? first-string) (empty? second-string)) 0
    (= (last first-string) (last second-string))
       ( + 1 (common-child-length
              (subs first-string 0 (- (count first-string) 1))
              (subs second-string 0 (- (count second-string) 1))))
    :else (max (common-child-length first-string 
                      (subs second-string 0 (- (count second-string) 1)))
               (common-child-length (subs first-string 0 (- (count first-string) 1))
                      second-string))
    ))

