(ns otus-04.homework.scramblies)

;; Оригинальная задача:
;; https://www.codewars.com/kata/55c04b4cc56a697bb0000048


(defn count-chars [chars]
  (reduce (fn [acc c]
            (update acc c (fnil inc 0)))
          {}
          chars))


(defn scramble?
  "Функция возвращает true, если из букв в строке letters
  можно составить слово word."
  [letters word]
  (let [letters-map (count-chars letters)
        word-map    (count-chars word)]
    (every? (fn [[k v]]
              (<= v (letters-map k 0)))
            word-map)))

