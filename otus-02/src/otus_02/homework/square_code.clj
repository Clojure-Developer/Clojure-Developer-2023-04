(ns otus-02.homework.square-code
  (:require [clojure.string :as s]))

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

(defn square-code
  "Кодирует или декодирует текст квадратным кодом. Параметры:
  * text          - входная строка,
  * del-separator - удалять последний символ при формировании квадрата или нет,
  * add-separator - добавлять разделитель при объединении строк квадрата или нет."
  [text del-separator add-separator]
  (let [;; считаем получившуются длину
        lnth (count text)
        ;; определяем количество строк
        rows (int (Math/sqrt lnth))
        ;; определяем количество столбцов
        cols (+ rows (if (= lnth (* rows rows)) 0 1))
        ;; паддинг
        padd (s/join (repeat cols " "))
        ;; формируем исходную матрицу
        sqre (map (if del-separator
                    ;; при декодировании нужно удалить пробел, добавленный
                    ;; в момент объединения строк 
                    (comp s/join butlast)
                    ;; при кодировании просто объединяем строки
                    s/join)
                  (partition cols cols padd text))]
    ;; транспонируем матрицу и объединяем строки
    (s/join (if add-separator " " "")
            (map s/join (apply mapv vector sqre)))))


(defn encode-string
  "Кодирует сообщение квадратным кодом."
  [input]
  (-> input
      s/lower-case
      (s/replace #"[\p{Punct}\s]" "")
      (square-code false true)))

(defn decode-string
  "Декодирует сообщение, закодированное квадратным кодом."
  [input]
  (-> input
      (square-code true false)
      s/trimr))
