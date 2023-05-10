(ns otus-02.homework.square-code
  (:require [otus-02.homework.palindrome :refer [normalize-str]]
            [clojure.string :as s]))

;; Реализовать классический метод составления секретных сообщений, называемый `square code`.
;; Выведите закодированную версию полученного текста.

;; Во-первых, текст нормализуется: из текста удаляются пробелы и знаки препинания,
;; также текст переводится в нижний регистр.
;; Затем нормализованные символы разбиваются на строки.
;; Эти строки можно рассматривать как образующие прямоугольник при печати их друг под другом.

;; Например,
"If man was meant to stay on the ground, god would have given us roots."
;; нормализуется в строку:
"ifmanwasmeanttostayonthegroundgodwouldhavegivenusroots"

;; Разбиваем текст в виде прямоугольника.
;; Размер прямоугольника (rows, cols) должен определяться длиной сообщения,
;; так что c >= r и c - r <= 1, где c — количество столбцов, а r — количество строк.
;; Наш нормализованный текст имеет длину 54 символа
;; и представляет собой прямоугольник с c = 8 и r = 7:
"ifmanwas"
"meanttos"
"tayonthe"
"groundgo"
"dwouldha"
"vegivenu"
"sroots  "

;; Закодированное сообщение получается путем чтения столбцов слева направо.
;; Сообщение выше закодировано как:
"imtgdvsfearwermayoogoanouuiontnnlvtwttddesaohghnsseoau"

;; Полученный закодированный текст разбиваем кусками, которые заполняют идеальные прямоугольники (r X c),
;; с кусочками c длины r, разделенными пробелами.
;; Для фраз, которые на n символов меньше идеального прямоугольника,
;; дополните каждый из последних n фрагментов одним пробелом в конце.
"imtgdvs fearwer mayoogo anouuio ntnnlvt wttddes aohghn  sseoau "

;; Обратите внимание, что если бы мы сложили их,
;; мы могли бы визуально декодировать зашифрованный текст обратно в исходное сообщение:

"imtgdvs"
"fearwer"
"mayoogo"
"anouuio"
"ntnnlvt"
"wttddes"
"aohghn "
"sseoau "

(defn size-field [len-str]
  (let [x (int (Math/sqrt len-str))]
    (cond 
      (= len-str (* x x)) [x x]
      (<= len-str (* x (inc x))) [(inc x) x]
      :else [(inc x) (inc x)])))
      


(defn mix [v r]
  (for [i (range r)]
      (map #(nth % i "") v)))

(defn l->str [l]
  (map #(apply str %) l)
  )

(defn encode-string [input]
  (let [n-input (normalize-str input)
        [r _] (size-field (count n-input))
        v-input (partition r r (cycle " ") n-input)
        l-mix (mix v-input r)
        s-mix (l->str l-mix)]
    (s/join " " s-mix)))

(encode-string "If man was meant to stay on the ground, god would have given us roots.")

(defn decode-string [input]
  (let [l-input (s/split input #" ")
        r (count (get l-input 0))
        v-input (mix l-input r)
        s-input (l->str v-input)]
    (apply str (normalize-str (s/join s-input)))))

(decode-string "imtgdvs fearwer mayoogo anouuio ntnnlvt wttddes aohghn  sseoau ")