(ns otus-04.homework.scramblies)

;; Оригинальная задача:
;; https://www.codewars.com/kata/55c04b4cc56a697bb0000048

(defn collect-stats [step initial-stats elems]
  (reduce
    (fn [stat key]
      (let [value (get stat key 0)]
        (assoc stat key (step value))))
    initial-stats
    elems))

(def count-letters (partial collect-stats inc {}))
(def subtract-letters (partial collect-stats dec))

(defn scramble?
  "Функция возвращает true, если из букв в строке letters
  можно составить слово word."
  [letters word]
  (let [stat-letters (count-letters letters)
        result-stat  (subtract-letters stat-letters word)
        with-neg-values (filter (fn [[_ v]] (neg? v)) result-stat)]
    (zero? (count with-neg-values))))

