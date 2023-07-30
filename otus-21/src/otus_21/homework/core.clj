(ns otus-21.homework.core
  (:require [clojure.zip :as z]
            [clojure.walk :as w]))

;; * Декодер

(defn decode-line [line]
  (cond (clojure.string/starts-with? line "$ cd")
        (let [name (clojure.string/replace-first line "$ cd " "")]
          [:cd (if (= name "..") :up name )])

        (java.lang.Character/isDigit (first line))
        (let [[size name] (clojure.string/split line #" " 2)]
          [:file [name (java.lang.Integer/decode size)]])))

(defn decode [input]
  (->> input
       clojure.string/split-lines
       rest  ;; нам не интересен переход в корень
       (map decode-line)
       (filter (complement nil?))))

;; * Зиппер

(defn fs-zipper [root]
  (z/zipper
   (constantly true)
   :subdirs
   (fn [dir ds] (assoc dir :subdirs ds))
   root))

(defn mkdir [name]
  {:name name
   :files []
   :subdirs []})

(defn cd [loc name]
  (if (empty? (z/children loc))
    (-> loc
        (z/insert-child (mkdir name))
        z/down)
    (loop [cursor (z/leftmost (z/down loc))]
      (if (= name (:name (z/node cursor)))
        cursor
        (if-let [new-loc (z/right cursor)]
          (recur new-loc)
          (-> cursor
              (z/insert-right (mkdir name))
              z/right))))))

(defn file [loc name]
  (z/edit loc update :files conj name))

(comment
  ;; пользуемся так:
  (-> (fs-zipper (mkdir ""))
      (cd "a")
      (file "foo.txt")
      (file "bar.zip")
      z/up
      (cd "b")
      (file "files.lst")
      (file "out.txt")
      (file "data.bin")
      z/up
      (file "README.md")
      (cd "a")
      (cd "aa")
      (file "main.c")
      (file "main.h")
      z/root))

;; * Интерпретатор команд

(defn build-fs [commands]
  (z/root
   (reduce
    (fn [loc [cmd arg]]
      (if (= cmd :file)
        (file loc arg)
        (if (= arg :up)
          (z/up loc)
          (cd loc arg))))
    (fs-zipper (mkdir ""))
    commands)))

;; Эта функция просто распечатывает дерево
(defn print-fs [fs]
  (loop [cursor (fs-zipper fs)]
    (when-not (z/end? cursor)
      (when-let [n (z/node cursor)]
        (let [p (clojure.string/join (repeat (count (z/path cursor)) " "))]
          (println (str p (:name n) "/"))
          (doseq [f (:files n)]
            (println (str p " " f)))))
      (recur (z/next cursor)))))

(defn measure [fs]
  (->> fs
       ;; сначала избавляемся от списков файлов, чтобы не обходить ещё и их
       ;; для этого подходит prewalk
       (w/prewalk
        (fn [n]
          (if-let [files (:files n)]
            [;; сумма размеров файлов
             (->> files
                  (map second)
                  (reduce +))
             ;; поддиректории
             (:subdirs n)]
            n)))
       ;; на этом этапе у нас есть структура вида
       ;; [размер-файлов
       ;;  ([размер-файлов (...)]
       ;;   [размер-файлов (...)])]
       (w/postwalk
        (fn [n]
          (if (and (vector? n) (not (empty? n)))
            (let [[files subdirs] n]
              [(reduce + files (map first subdirs))
               subdirs])
            n)))
       ;; а к этому моменту размеры файлов дополнены размерами поддиректорий
       ))

(defn sum-of-sizes [input]
  "По журналу сеанса работы в терминале воссоздаёт файловую систему
и подсчитывает сумму размеров директорий, занимающих на диске до
100000 байт (сумма размеров не учитывает случай, когда найденные
директории вложены друг в друга: размеры директорий всё так же
суммируются)."
  (->> input
       decode
       build-fs
       measure
       flatten ;; да, тут уже можно, потому что в дереве остались только числа
       (filter #(< % 100000))
       (reduce +)))
