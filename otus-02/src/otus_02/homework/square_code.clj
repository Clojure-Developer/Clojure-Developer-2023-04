(ns otus-02.homework.square-code
  (:require [clojure.string :as str]))

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

(defn is-letter [c] (Character/isLetter c))
(defn normalize [s]  (filter is-letter (str/lower-case s)))

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

;; Чтение стобцов слева-направо в строку
(defn read-rectangle [rectangle]
  (apply str
    (loop [result '()
           rectangle rectangle]
      (if (empty? (first rectangle))
        result
        (recur
          (concat result (map first rectangle))
          (map rest rectangle))))))

;; Закодированное сообщение получается путем чтения столбцов слева направо.
;; Сообщение выше закодировано как:
"imtgdvsfearwermayoogoanouuiontnnlvtwttddesaohghnsseoau"
(defn encode-helper [input]
  (let [normalized (normalize input)
        cnt        (count normalized)
        r          (int (Math/sqrt cnt))
        c          (if (< (* r r) cnt) (inc r) r)
        rectangle  (partition-all c normalized)]
    (read-rectangle rectangle)))

;; Полученный закодированный текст разбиваем кусками, которые заполняют идеальные прямоугольники (r X c),
;; с кусочками c длины r, разделенными пробелами.
;; Для фраз, которые на n символов меньше идеального прямоугольника,
;; дополните каждый из последних n фрагментов одним пробелом в конце.
"imtgdvs fearwer mayoogo anouuio ntnnlvt wttddes aohghn  sseoau "
(defn encode-string [input]
  (let [
        input              (encode-helper input)
        cnt                (count input)
        cols-cnt           (int (Math/sqrt cnt))
        rows-cnt           (if (< (* cols-cnt cols-cnt) cnt) (inc cols-cnt) cols-cnt)
        incomplete-cnt     (- (* rows-cnt cols-cnt) cnt)
        complete-cnt       (- rows-cnt incomplete-cnt)
        sub-str-cnt        (* complete-cnt cols-cnt)
        complete-sub-str   (subs input 0 sub-str-cnt)
        incomplete-sub-str (subs input sub-str-cnt)
        complete-slices    (partition-all cols-cnt complete-sub-str)
        incomplete-slices  (partition-all (dec cols-cnt) incomplete-sub-str)]
    (str/join
      " "
      (concat
        (map #(apply str %) complete-slices)
        (map #(apply str (concat % '(\space))) incomplete-slices)))))

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

(defn decode-string [input]
  (read-rectangle (str/split input #"\s+")))

