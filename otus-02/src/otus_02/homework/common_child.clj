(ns otus-02.homework.common-child
  (:require [clojure.string :as s]
            [clojure.set :refer [intersection]]))


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

(defn childs
  "Ищет прямых потомков указанной строки, т.е. таких, которые получаются
  одной делецией."
  [text]
  (map (fn [i] (s/join (concat (take i text) (drop (inc i) text))))
       (range (count text))))

(defn descendands
  "Ищет всех возможных потомков указанной строки."
  [text]
  ;; Сохраняем найденных потомков в стек. Проходим по этому стеку и ищем 
  ;; прямых потомков для каждого потомка и т.д.
  (loop [stack #{text} acc #{}]
    ;; Стек кончился, возвращаем результат.
    (if (empty? stack) acc
      (let [;; Следующий элемент, для которого будем искать прямых потомков.
            next-string (apply max-key count stack)
            ;; Прямые потомки следующего элемента.
            next-childs (childs next-string)]
        ;; Элемент обработан, убираем его из стека и добавляем в общий список.
        ;; Его прямых потомков добавляем в стек.
        (recur (apply conj (disj stack next-string) next-childs)
               (conj acc next-string))))))

(defn common-child-length
  "Определяет общего потомка двух строк с максимальной длиной и возвращает её."
  [first-string second-string]
  (count (apply max-key count (intersection (descendands first-string)
                                            (descendands second-string)))))
