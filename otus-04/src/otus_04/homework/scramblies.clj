(ns otus-04.homework.scramblies)

;; Оригинальная задача:
;; https://www.codewars.com/kata/55c04b4cc56a697bb0000048

(defn my-frequencies [coll]
  (reduce (fn [acc x] (update acc x (fnil inc 0))) {} coll))

(defn scramble?
  "Функция возвращает true, если из букв в строке letters
  можно составить слово word."
  [letters word]
  (let [letters-map (my-frequencies letters)
        word-map (my-frequencies word)]
    (every? (fn [[key value]] (>= (get letters-map key 0) value)) word-map)))
