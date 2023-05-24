(ns otus-04.homework.crossword-puzzle
  (:require [clojure.string :as str]))

(defn split-by-mask [words mask]
  (let [grouped (group-by #(boolean (re-matches mask %)) words)]
    [(get grouped true) (get grouped false)]))

(defn make-mask
  "Переводим вектор (список) вида 'M--C--' в регулярное выражение"
  [mask]
  (re-pattern (str/replace (apply str mask) "-" "[A-Z]")))

(defn insert-into
  "Вставляем в вектор символов символы chars начиная с позиции i,
  заменяя каждый символ вектора на очередной символ из chars"
  [input i chars]
  (vec (concat (take i input) chars (drop (+ i (count chars)) input))))

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

(defn search [input ch] (.indexOf input ch))
(defn find-free-cell
  "Ищем первую свободную ячейку '-'"
  [board]
  (let [n (count board)]
    (loop [row 0
           result nil]
      (if (or (some? result) (>= row n))
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

(defn vertical?
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
          (if (= cell \+)
            (inc new-col)
            (recur (dec new-col))))
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
         (if (= cell \+)
           (inc new-row)
           (recur (dec new-row))))
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

(defn get-blank-line
  [board row col]
  (cond
    (horizontal? board row col)
    (let [start (get-horizontal-start board row col)]
      {:line (get-horizontal board row start) :type 'horizontal :row row :col start})

    (vertical? board row col)
    (let [start (get-vertical-start board row col)]
      {:line (get-vertical board start col) :type 'vertical :row start :col col})

    :else nil))

(defn calc-next-move
  [board words]
  (let [[row col]         (find-free-cell board)
        blank-line-result (get-blank-line board row col)]
    (if (some? blank-line-result)
      (let [{line :line type :type row :row col :col} blank-line-result
            mask                    (make-mask line)
            [matches other]         (split-by-mask words mask)]
        {:matches matches :other other :type type :row row :col col})
      nil)))

(defn make-next-move
  [board words]
  (let [state (calc-next-move board words)
        {[first-match & rest-matches] :matches
         type :type
         row :row
         col :col} state]
    (if (some? first-match)
      (let [new-board
            (if (= type 'horizontal)
              (update-horizontal board row col first-match)
              (update-vertical board row col first-match))]
        (merge state {:board new-board :matches rest-matches}))
      nil)))

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
