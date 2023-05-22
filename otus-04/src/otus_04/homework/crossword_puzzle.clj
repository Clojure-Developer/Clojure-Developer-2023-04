(ns otus-04.homework.crossword-puzzle
  (:require [clojure.string :as str]))

(defn group-by-matches [words mask]
  (group-by (partial re-matches mask) words))
(defn make-mask
  "Переводим вектор (список) вида 'M--C--' в регулярное выражение"
  [mask]
  (re-pattern (str/replace (apply str mask) "-" "[A-Z]")))

(defn insert-into
  "Вставляем в вектор символов символы chars начиная с позиции i,
  заменяя каждый символ вектора на очередной символ из chars"
  [input i chars]
  (concat (take i input) chars (drop (+ i (count chars)) input)))

(defn update-horizontal
  "Обновляем в поле горизонтальную линию словом word, начиная с позиции row col
  Поле - вектор векторов"
  [board row col word]
  (let [board-row (get board row)
        new-board-row (insert-into board-row col word)]
    (assoc board row new-board-row)))

(defn update-vertical
  "Обновляем в поле вертикальную линию словом word, начиная с позиции row col
  Поле - вектор векторов"
  [board row col word]
  (loop [current-board board
         current-row row
         [ch & chars] word]
    (if (nil? ch)
      current-board
      (let [board-row (get board current-row)
            new-board-row (assoc board-row col ch)]
        (recur
          (assoc current-board current-row new-board-row)
          (inc current-row)
          chars)))))

(defn parse-input [input]
  (let [lines (str/split-lines input)
        board (vec (map vec (butlast lines)))
        words (str/split (last lines) #";")]
    {:board board :words words}))

(defn search [input ch]
  (let [pos (.indexOf input ch)]
    (if (= pos -1) nil pos)))
(defn find-free-cell
  "Ищем первую свободную ячейку '-'"
  [board]
  (let [n (count board)]
    (loop [row 0
           result nil]
      (if (or (not (nil? result)) (>= row n))
        result
        (let [board-row (get board row)
              col (search board-row \-)
              cell (if (> col -1) [row col] nil)]
          (recur (inc row) cell))))))

(defn valid-position?
  [board row col]
  (let [n (count board)]
    (and (< row n) (< col n) (>= row 0) (>= col 0))))

(defn horizontal?
  "Находимся ли мы на горизинтальной линии кросворда,
  предполагая row и col это свободная ячейка '-'"
  [board row col]
  (let [n (count board)]
    (if (valid-position? board row col)
      (let [cell         (get-in board [row col])
            nearest-col  (if (< col (dec n)) (inc col) (dec col))
            nearest-cell (get-in board [row nearest-col])]
        (and (= cell \-) (= nearest-cell \-)))
      false)))

(defn veritcal?
  "Находимся ли мы на вертикальной линии кросворда,
  предполагая row и col это свободная ячейка '-'"
  [board row col]
  (let [n (count board)]
    (if (valid-position? board row col)
      (let [cell              (get-in board [row col])
            nearest-row       (if (< row (dec n)) (inc row) (dec row))
            nearest-cell      (get-in board [nearest-row col])]
        (and (= cell \-) (= nearest-cell \-)))
      false)))

(defn get-horizontal-start
  [board row col]
  (if (and
        (valid-position? board row col)
        (= (get-in board [row col]) \-))
    (loop [new-col col]
      (if (valid-position? board row new-col)
        (let [cell (get-in board [row new-col])]
          (if (= cell \-)
            (recur (dec new-col))
            (inc new-col)))
        0))
    nil))

(defn get-vertical-start
  [board row col]
  (if (and
       (valid-position? board row col)
       (= (get-in board [row col]) \-))
   (loop [new-row row]
     (if (valid-position? board new-row col)
       (let [cell (get-in board [new-row col])]
         (println [new-row col] cell)
         (if (= cell \-)
           (recur (dec new-row))
           (inc new-row)))
       0))
   nil))

(defn get-horizontal
  "Вычитываем текущую горизонтальную линию кросворда в строку, где row col ее валидное начало"
  [board row col]
  (let [board-row (get board row)
        start-from-col (drop col board-row)]
    (take-while (partial not= \+) start-from-col)))

(defn get-vertical
  "Вычитываем вертикальную горизонтальную линию кросворда в строку, где row col ее валидное начало"
  [board row col]
  (loop [new-row row
         vertical []]
    (let [cell (get-in board [new-row col] \+)]
      (if (= cell \+)
        vertical
        (recur (inc new-row) (conj vertical cell))))))

(def input "+-++++++++
+-++++++++
+-++++++++
+-----++++
+-+++-++++
+-+++-++++
+++++-++++
++------++
+++++-++++
+++++-++++
LONDON;DELHI;ICELAND;ANKARA")
(def board (:board (parse-input input)))

;; Идем либо в горизноталь либо в вертикаль - в любом случае получаем список слов которые "подходят" и оставшиеся
;; Возварщаем так же "тип" (вертикаль или горизонталь)
;; Если тип не подходит - возвращаем подходящие как пустой список
;; Потом (в другой функции) перебираем подходящие, вставляя в борду и рекурсиво запускаем солвер на U от оставшиеся подходящие + оставшиеся
(defn make-one-step
  [board words]
  (let [[row col] (find-free-cell board)]
    (cond
      ((horizontal? board row col)
       (let [start      (get-horizontal-start board row col)
             horizontal (get-horizontal board row start)
             mask       (make-mask horizontal)
             matches    (group-by-matches words mask)])))

    (println row col)))


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
  "")







