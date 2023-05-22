(ns otus-04.homework.scramblies)


;; Оригинальная задача:
;; https://www.codewars.com/kata/55c04b4cc56a697bb0000048

(defn check-char [acc ch]
  (let [idx (.indexOf acc ch)]
    (if (>= idx 0)
      (assoc acc idx \-)
      (reduced false))))

(defn scramble?
  "Функция возвращает true, если из букв в строке letters
  можно составить слово word."
  [letters word]
  (boolean (reduce check-char (vec letters) (vec word))))

(comment
  
  (scramble? "rkqodlw" "world"))


  
