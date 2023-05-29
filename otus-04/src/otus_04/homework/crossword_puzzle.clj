(ns otus-04.homework.crossword-puzzle
  (:require [clojure.string :as str]))

(defn split-by-mask
  "Разбиваем список слов на 2 группы:
    - слова, которые совпадают под маску
    - слова, которые не совпадают под маску"
  [words mask]
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

(defn add-word-horizontally
  "Добавляем в поле горизонтально слово word, начиная с позиции row col
  Поле - вектор векторов"
  [board row col word]
  (let [board-row (get board row)
        new-board-row (insert-into board-row col word)]
    (assoc board row new-board-row)))

(defn update-cell
  [board row col ch]
  (let [board-row (get board row)
        new-board-row (assoc board-row col ch)]
    (assoc board row new-board-row)))

(defn add-word-vertically
  "Добавляем в поле вертикально слово word, начиная с позиции row col
  Поле - вектор векторов"
  [board row col word]
  (->> word
       (map list (iterate inc row))
       (reduce
         (fn [result-board [current-row ch]]
           (update-cell result-board current-row col ch))
         board)))

(defn parse-input
  "Парсим входные строку программы и возвращаем поле и список слов в соот-щих ключах мапы"
  [input]
  (let [lines (str/split-lines input)
        board (vec (map vec (butlast lines)))
        words (str/split (last lines) #";")]
    {:board board :words words}))

(defn str->board
  "Строковое представление поля кроссворда"
  [board]
  (str/join "\n" (map (partial str/join) board)))

(defn search [input ch] (.indexOf input ch))

(defn find-free-cell
  "Ищем первую свободную ячейку (то есть ту, которая символизиурется прочерком '-')"
  [board]
  (->> board
       (map (fn [row board-row]
              (let [col (search board-row \-)
                    ok? (>= col 0)]
                {:row row :col col :ok? ok?}))
            (iterate inc 0))
       (filter #(:ok? %))
       first))

(defn valid-position?
  "Валидна ли позиция ячейки (row col) на поле?"
  [board row col]
  (let [n (count board)]
    (and (< row n) (< col n) (>= row 0) (>= col 0))))

(defn on-the-line?
  [board row col get-nearest-cell-coordinates]
  (and
    (valid-position? board row col)
    (let [cell (get-in board [row col])
          [r c] (get-nearest-cell-coordinates row col)
          nearest-cell (get-in board [r c])]
      (and (= cell \-) (= nearest-cell \-)))))

(defn horizontal?
  "Находимся ли мы на свободной горизонтальной линии кроссворда,
  предполагая row и col указывают на свободную ячейку (то есть '-')"
  [board row col]
  (on-the-line? board row col
                (fn [r c] [r (if (< c (dec (count board))) (inc c) (dec c))])))

(defn vertical?
  "Находимся ли мы на вертикальной линии кроссворда,
  предполагая row и col указывают на свободную ячейку (то есть '-')"
  [board row col]
  (on-the-line? board row col
                (fn [r c] [(if (< r (dec (count board))) (inc r) (dec r)) c])))
(defn get-start
  [board row col on-next on-final]
  (when (and
          (valid-position? board row col)
          (= (get-in board [row col]) \-))
    (loop [[new-row new-col] [row col]]
      (if (valid-position? board new-row new-col)
        (let [cell (get-in board [new-row new-col])]
          (if (= cell \+)
            (on-final new-row new-col)
            (recur (on-next new-row new-col))))
        0))))

(defn get-horizontal-start
  "Находим начало горизонтальной линии, на которую указывает row col,
  предполагая, что row и col указывают на свободную ячейку"
  [board row col]
  (get-start board row col
             (fn [row col] [row (dec col)])
             (fn [_ col] (inc col))))

(defn get-vertical-start
  "Находим начало вертикальной линии, на которую указывает row col,
  предполагая, что row и col указывают на свободную ячейку"
  [board row col]
  (get-start board row col
             (fn [row col] [(dec row) col])
             (fn [row _] (inc row))))

(defn get-horizontal
  "Вычитываем текущую горизонтальную линию кроссворда (необязательно полностью свободную) в строку,
  где row col ее валидное начало"
  [board row col]
  (let [board-row (get board row)
        start-from-col (drop col board-row)]
    (take-while (partial not= \+) start-from-col)))

(defn get-vertical
  "Вычитываем текущую вертикальную линию кроссворда (необязательно полностью свободную) в строку,
  где row col ее валидное начало"
  [board row col]
  (loop [new-row row
         vertical []]
    (let [cell (get-in board [new-row col] \+)]
      (if (= cell \+)
        vertical
        (recur (inc new-row) (conj vertical cell))))))

(defn get-free-line
  "Вычитываем линию кроссворда в строку (необязательно полностью свободную), где row col ее валидное начало"
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
  "Просчитываем состояние следующего шага в дереве решения
  Вернет список подходящих слов, список оставшихся слов, тип ориентации слова и координаты ячейки для вставки слова"
  [board words]
  (let [{:keys [row col ok?]} (find-free-cell board)]
    (if ok?
      (when-let [blank-line-result (get-free-line board row col)]
        (let [{line :line type :type row :row col :col} blank-line-result
              mask (make-mask line)
              [matches other] (split-by-mask words mask)]
          {:matches matches :other other :type type :row row :col col}))
      nil)))

(defn make-one-move
  "Делаем один ход определенного типа с текущим подходящим словом начиная с координаты row col.
  Вернем новую доску"
  [board word type row col]
  (if (= type 'horizontal)
    (add-word-horizontally board row col word)
    (add-word-vertically board row col word)))

(declare do-solve)

(defn do-solve-next-move
  "Находим решение для текущей доски и просчитанного состояния для следующего шага"
  [board next-move-state]
  (let [{:keys [matches other type row col]} next-move-state]
    (loop [[first-match & rest-matches] matches
           other-words other]
      (if (some? first-match)
        (let [rest-words (concat rest-matches other-words)
              ;; обновили доску подходящим словом в нужном месте по нужному направлению
              new-board (make-one-move board first-match type row col)
              ;; теперь рекурсивно пробуем решить доску на всех оставшихся словах
              solve-result (do-solve new-board rest-words)]
          (if (:ok? solve-result)
            ;; если получилось решить - значит расположение первого подоходящего слова рекурсиво решило всю доску
            solve-result
            ;; иначе пробуем следующее подходящее слово
            (recur
              rest-matches
              ;; задвинув слово на котором только что попробовали вконец списка
              (concat other-words [first-match]))))
        {:board board :ok? false}))))

(defn do-solve
  "Решаем кроссворд для текущей доски и общего списка слов, которым мы располагаем на данный момент"
  [board words]
  (if (pos? (count words))
    (let [next-move-state (calc-next-move board words)
          {matches :matches} next-move-state]
      (if (some? matches)
        ;; нашли подходящие слова, которые могут привести нас к решению -
        ;; пытаемся обходить это слова и продвигаться дальше в дереве решений
        (do-solve-next-move board next-move-state)
        ;; если не получилось найти подходящие слова, которые продвинули бы нас дальше в дереве решений,
        ;; то значит и нельзя решит текущую доску с текущими вариантами слов
        {:board board :ok? false}))
    ;; если все слова перебрали, решение есть
    {:board board :ok? true}))


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
  (let [{board :board words :words} (parse-input input)
        solve-result (do-solve board words)
        {ok? :ok? board :board} solve-result]
    (if ok? (str->board board) "")))
