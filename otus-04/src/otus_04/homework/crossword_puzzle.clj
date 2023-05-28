(ns otus-04.homework.crossword-puzzle
  (:require [clojure.string :as str]))

;; Оригинал:
;; https://www.hackerrank.com/challenges/crossword-puzzle/problem

(defn parse-input [input]
  (let [lines (map str/trim (str/split-lines input))
        words-row (last lines)
        words (str/split words-row #");")
        puzzle (butlast lines)]
    [puzzle words]))

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
  (let [[puzzle words] (parse-input input)]))
