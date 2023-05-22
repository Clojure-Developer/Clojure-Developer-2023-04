(ns otus-04.homework.scramblies)

;; Оригинальная задача:
;; https://www.codewars.com/kata/55c04b4cc56a697bb0000048

(defn count-letters [word]
  (reduce
    (fn [stat ch]
      (let [cnt (get stat ch 0)]
        (assoc stat ch (inc cnt))))
    {}
    word))

(defn de-count-letters [init-stat word]
  (reduce
    (fn [stat ch]
      (let [cnt (get stat ch 0)]
        (assoc stat ch (dec cnt))))
    init-stat
    word))

(defn scramble?
  "Функция возвращает true, если из букв в строке letters
  можно составить слово word."
  [letters word]
  (let [stat-letters (count-letters letters)
        result-stat  (de-count-letters stat-letters word)
        with-neg-values (filter (fn [[_ v]] (neg? v)) result-stat)]
    (zero? (count with-neg-values))))

