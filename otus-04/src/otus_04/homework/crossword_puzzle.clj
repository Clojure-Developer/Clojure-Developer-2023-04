(ns otus-04.homework.crossword-puzzle
  (:require [clojure.string :as s]
            [clojure.set :as set]))

;; Оригинал:
;; https://www.hackerrank.com/challenges/crossword-puzzle/problem

(defn find-places-in-line
  "Возвращает список словомест в текущей строке. Каждое словоместо определяется
  координатами первого и последнего символа."
  [i line]
  ;; Проходим по линиям в цикле.
  (loop [line line acc [] cur 0]
    ;; В каждой линии определяем участки из последовательности минусов.
    ;; Если такая последовательность состоит более чем из одного символа,
    ;; запоминаем ее начало и конец.
    (let [line-rest (drop-while (partial = \+) line)
          line-next (drop-while (partial = \-) line-rest)
          beg (+ cur (count line) (- (count line-rest)))
          end (+ cur (count line) (- (count line-next)) -1)]
      (if (empty? line-rest) acc
        (recur line-next (if (zero? (- end beg)) acc
                           (conj acc [[i beg] [i end]]))
               (inc end))))))

(defn find-places
  "Возвращает список горизонтальных словомест в наборе строк."
  [lines]
  (apply concat (filter seq (map-indexed find-places-in-line lines))))

(defn invert-row-col
  "В списке пар координат меняет строку и столбец местами."
  [places]
  (map #(map (fn [[c r]] (vector r c)) %) places))

(defn transpose-lines
  "Транспонирует матрицу, представленную списокм строк."
  [lines]
  (apply mapv #(s/join "" %&) lines))

(defn parse-crossword
  "Парсит исходное поле кроссворда, возвращает набор словомест."
  [lines]
  (concat (find-places lines)
          ;; Для определения вертикальных словомест транспонируем поле кроссворда.
          (invert-row-col
            (find-places (transpose-lines lines)))))

(defn parse
  "Парсит исходные данные, возвращает набор словомест и набор слов."
  [data]
  (let [;; Сначала получаем исходное поле и список слов.
        [field words] (-> data
                          s/split-lines
                          ((juxt #(-> % butlast)
                                 #(-> % last (s/split #";")))))]
    ;; Возвращаем исходное поле, распаршенное исходное поле (в виде
    ;; набора диапазонов координат) и список слов.
    [field (parse-crossword field) words]))

(defn permutations
  "Возвращает все возможные перестановки элементов списка."
  [elements]
  ;; Слева список начальных элементов, справа - список возможных продолжений.
  (loop [els [[[] elements]]]
    ;; Если правый список для всех вариантов пуст, выходим из рекурсии. Хотя
    ;; не обязательно проверять все.
    (if (every? (comp empty? second) els) (map first els)
      ;; Перебираем возможные следующие элементы, добавляем их по отдельности к
      ;; начальному списку, получаем новые варианты начального списка.
      (let [perm (map (fn [[pref suff]]
                        (map (fn [[next rest]]
                               [(conj pref next) rest])
                             ;; Проходим по всем возможным следующих элементам и для каждого возвращаем
                             ;; пару: сам элемент и список оставшихся.
                             (map-indexed (fn [i el]
                                            [el (concat (take i suff) (drop (inc i) suff))])
                                          suff)))
                      els)]
        ;; Убираем вложенность.
        (recur (apply concat perm))))))

(defn check-order
  "Проверяет, исходя из длины, что слова в списке расположены в порядке, подходящем под
  словоместа."
  [places words]
  (every? true? (map (fn [[beg end] word]
                       (= (count word) (apply + 1 (mapv - end beg))))
                     places words)))

(defn place-range
  "Возвращает координаты всех символов в диапазоне."
  [place]
  (let [[[r0 c0] [r1 c1]] place]
    (if (= r0 r1) (mapv #(vector r0 %) (range c0 (inc c1)))
      (mapv #(vector % c0) (range r0 (inc r1))))))

(defn check-intersection
  "Проверяет, что на пересечении двух слов стоят одинаковые символы."
  [word1 word2]
  ;; Находим точку пересечения двух слов, если такая есть.
  ;; Если слова не пересекаются, every? вернет true.
  (let [positions (set/intersection (set (keys word1)) (set (keys word2)))]
    ;; Проверяем, что на пересечениях (хотя пересечение одно) в обоих словах
    ;; стоят одинаковые символы.
    (every? (fn [pos]
              (= (word1 pos) (word2 pos)))
            positions)))

(defn check-crossword
  "Проверяет корректность решения кроссворда."
  [crossword]
  ;; Проверяем пересечения каждого слова с каждым.
  (loop [words crossword]
    (cond
      (empty? (rest words)) true
      (not-every? (partial check-intersection (first words)) (rest words)) false
      :else (recur (rest words)))))

(defn restore-field
  "Восстанавливает исходное поле кроссворда, добавляя на него слова."
  [field crossword]
  (->> crossword
       ;; Объединяем привязанные к координатам символы в общий список.
       (reduce conj)
       ;; Помещаем символы на поле кроссворда.
       (reduce (fn [field [pos c]]
                 (assoc-in field pos c))
               (mapv vec field))
       (mapv s/join)
       (s/join "\n")))



(defn solve
  "Возвращает решённый кроссворд. Аргумент является строкой вида

  +-++++++++
  +-++++++++
  +-++++++++
  +-----++++
  +-+++-++++
  +-+++-++++
  +++++-++++
  ++------++
  +++++-++++
  +++++-++++
  LONDON;DELHI;ICELAND;ANKARA

  Все строки вплоть до предпоследней описывают лист бумаги, а символами
  '-' отмечены клетки для вписывания букв. В последней строке перечислены
  слова, которые нужно 'вписать' в 'клетки'. Слова могут быть вписаны
  сверху-вниз или слева-направо."
  [input]
  (let [;; Парсим исходные данные.
        [field places-ranges words] (parse input)
        ;; Формируем перестановки слов и оставляем только подходящие по
        ;; длинам к словоместам.
        words-permutations (filter (partial check-order places-ranges)
                                   (permutations words))
        ;; Преобразуем словоместа в наборы координат.
        places (map place-range places-ranges)
        ;; Для каждой перестановки слов генерируем кроссворд (привязываем
        ;; символы к координатам).
        crosswords (map (partial map zipmap places) words-permutations)]
    (restore-field field (first (filter check-crossword crosswords)))))
