(ns otus-04.homework.scramblies
  (:require [clojure.set :as s]))

;; Оригинальная задача:
;; https://www.codewars.com/kata/55c04b4cc56a697bb0000048

(defn- ->set
  "Превращает последовательность в множество пар из элементов
  последовательности и их повторов в ней (начиная с нуля)."
  [items]
  (first
   (reduce (fn [[result counts] i]
             (let [v (get counts i 0)]
               [(conj result [i v])
                (assoc counts i (inc v))]))
           [#{} {}]
           items)))

(comment
  ;; Примеры использования
  (->set "aaa")  ;; #{[\a 0] [\a 1] [\a 2]}
  (->set "aba")  ;; #{[\a 0] [\b 0] [\a 1]}
  )

(defn scramble?
  "Функция возвращает true, если из букв в строке letters
  можно составить слово word."
  [letters word]
  (s/superset? (->set letters)
               (->set word)))
