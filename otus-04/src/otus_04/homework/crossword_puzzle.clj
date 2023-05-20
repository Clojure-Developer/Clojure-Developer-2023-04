(ns otus-04.homework.crossword-puzzle
  (:require [clojure.string :as str]))

;; Оригинал:
;; https://www.hackerrank.com/challenges/crossword-puzzle/problem

(defn neibs
  [[x y] dx dy spaces]
  (let [p [(+ x dx) (+ y dy)]]
    (if (spaces p)
      (cons p (neibs p dx dy spaces))
      ())))

(defn expand
  [pos dx dy spaces]
  (concat (reverse (neibs pos (- dx) (- dy) spaces))
          [pos]
          (neibs pos dx dy spaces)))

(defn spaces->targets
  [spaces]
  (set
   (for [pos spaces
         target [(expand pos 1 0 spaces)
                 (expand pos 0 1 spaces)]
         :when (> (count target) 1)]
     target)))

(defn put-word [field target word]
  (when (= (count target) (count word))
    (reduce
     (fn [f [pos c]]
       (if (not= (get f pos c) c)
         (reduced nil)
         (assoc f pos c)))
     field
     (map vector target word))))

(defn put-words [field targets words]
  (if (empty? words)
    field
    (let [[t & ts] targets]
      (first
       (for [w (seq words)
             :let [f (put-word field t w)]
             :when (not (nil? f))
             :let [r (put-words f ts (disj words w))]
             :when (not (nil? r))]
         r)))))

(defn read-input [input]
  (let [lines (str/split-lines input)]
    {:spaces
     (set (for [[y row] (map-indexed vector (drop-last lines))
                [x val] (map-indexed vector row)
                :when (= \- val)]
            [x y]))

     :words
     (set (str/split (last lines) #";"))

     :width
     (count (first lines))

     :height
     (dec (count lines))}))

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
  (let [{:keys [spaces words width height]} (read-input input)
        field (put-words {} (spaces->targets spaces) words)]
    (str/join
     \newline
     (for [y (range height)]
       (str/join
        (for [x (range width)]
          (get field [x y] \+)))))))
