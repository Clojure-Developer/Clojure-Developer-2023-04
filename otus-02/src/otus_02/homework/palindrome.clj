(ns otus-02.homework.palindrome
  (:require [clojure.string :as s]))

(defn is-palindrome
  "Проверяет, является ли строка палиндромом. Игнорирует регистр,
  знаки препинания и пробелы."
  [test-string]
  (let [text (-> test-string s/lower-case (s/replace #"[\p{Punct}\s]" ""))]
    (= text (s/reverse text))))
