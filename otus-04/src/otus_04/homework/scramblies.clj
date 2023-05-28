(ns otus-04.homework.scramblies)

;; Оригинальная задача:
;; https://www.codewars.com/kata/55c04b4cc56a697bb0000048

(defn scramble?
  "Функция возвращает true, если из букв в строке letters
  можно составить слово word."
  [letters word]
  (let [letters-map (frequencies letters)
        word-map (frequencies word)]
    (every? #(>= (get letters-map % 0) (get word-map % 0)) (keys word-map))))
