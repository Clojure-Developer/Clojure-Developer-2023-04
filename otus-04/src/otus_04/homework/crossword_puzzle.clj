(ns otus-04.homework.crossword-puzzle
  (:require [clojure.string :as str]))

;; Оригинал:
;; https://www.hackerrank.com/challenges/crossword-puzzle/problem

(defn parse-input [input]
  (let [v (str/split-lines input)]
    [
     (->> v
          (drop-last)
          (mapv vec))
     (->  v
          (last)
          (str/split #";")
          (#(map vec %))
          (set))]))
(defn cell? [field [x y]]
  (not= \+ (get-in field [y x] \+))
  )

(defn refine-pos 
  "Уточняет позицию начала слова, захватывая смежные буквы"
  ([x y field]
   (if (cell? field [(dec x) y])
     (refine-pos x y -1 0 field)
     (refine-pos x y 0 -1 field)
     ))
  ([x y dx dy field]
   (if (cell? field [(+ x dx) (+ y dy) ])
       (recur (+ x dx) (+ y dy) dx dy field)
       [x y])))

(defn find-free-pos
  "Найти первую незанятую позицию для слова"
  [field]
  (loop [idx 0]
    (let [x (.indexOf (field idx) \-)]
      (if (neg? x)
        (when-not (= idx (count field))
          (recur (inc idx)))
        (refine-pos x idx field)))))

(defn next-coord 
  "Координата следующей буквы в зависимости от направления"
  [[y x] vert?]
  (if vert? [(inc y) x] [y (inc x)]))
  

(defn insert-word 
  "Попробовать Вставить нужное слово по указанной позиции
   В случае удачи вернуть новое заполненное поле
   В случае фиаско - nil"
  [field [x y] word]
  (let [vert? (not= \- (get-in field [y (inc x)]))]
    (loop [field field
           coord (vector y x)
           word word]
      (let [f_ch (get-in field coord \+)
            c_ch (first word)]
        (cond
          (and (empty? word) (= f_ch \+)) field
          (empty? word) nil
          (or (= f_ch c_ch) (= f_ch \-))(recur 
                                         (assoc-in field coord c_ch)
                                         (next-coord coord vert?)
                                         (rest word))
          :else nil)))))



;; (defn match-word 
;;   "Подобрать слово для пустой позиции.
;;    В случае неудачи вернуть nil"
;;   [field words]
;;   (let [pos (find-free-pos field)
;;         size (count words)]
;;     (loop [idx 0]
;;       (let [new-field (insert-word field pos (words idx))]
;;         (if (nil? new-field)
;;           (if (< idx (dec size))
;;             ;; Слово не подошло. Есть еще. Пробуем дальше
;;             (recur (inc idx))
;;             ;; Слова кончились подходящего не найдено
;;             nil)
;;           (if (= 1 size)
;;             ;; Слово подошло и оно было последнее. Подобрали все! 
;;             new-field
;;             ;; Слово подошло углубляем поиск
;;             (let [new-field (match-word new-field (remove-vec words idx))]
;;               (if (nil? new-field)
;;                 (if (< idx size) (recur (inc idx)) nil)
;;                 new-field))))))))

(defn match-word [field words]
   (let [pos (find-free-pos field)]
     (loop [candidats words]
       (when-not (empty? candidats)
         (let [candidat (first candidats)
               new-field (insert-word field pos (first candidats))]
           (if (nil? new-field)
             ;; Слово не подошло. Пробуем дальше
             (recur (disj candidats candidat))
             (if (= 1 (count words))
               ;; Это было последнее слово. Все готово!
               new-field
               ;; Еще не весь кроссворд заполнен. Углубляемся
               (if-let [field-down (match-word new-field (disj words candidat))]
                 field-down
                 ;; Углубление не задалось. Ищем нового кандидата
                 (recur (disj candidats candidat)))
               )))))
  ))

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
  (let [[field words] (parse-input input)
        answer (match-word field words)]    
    (->> answer
        (map str/join)
        (str/join "\n"))))
