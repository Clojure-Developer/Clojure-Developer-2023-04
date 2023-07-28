(ns otus-21.homework.core
  (:require [clojure.string :as str]
            [clojure.zip :as zip]))

(defn starts-with?
  [prefix s]
  (str/starts-with? s prefix))

(defn make-fs-zipper
  []
  (zip/zipper
   (comp vector? :children)
   :children
   (fn [node children]
     (assoc node :children (vec children)))
   {:name "/" :size 0 :children []}))

(defn find-in-dir
  [z-loc name]
  (loop [loc (zip/down z-loc)]
    (if (= name (:name (zip/node loc)))
      loc
      (when-let [next (zip/right loc)]
        (recur next)))))

(defn build-tree
  [input]
  (let [lines (rest (str/split-lines input)) ;; we don't need first line
        zipper (make-fs-zipper)
        state :command]
    (loop [lines* lines
           zipper* zipper
           state* state]
      (if (empty? lines*)
        (zip/root zipper*)
        (condp = state*
          :command
          (let [[line & lines**] lines*]
            (condp starts-with? line
              "$ cd"
              (recur lines* zipper* :cd)
              "$ ls"
              (recur lines** zipper* :ls)
              (recur lines** zipper* :command)))

          :cd
          (let [[line & lines**] lines*
                name (str/replace line #"\$ cd " "")]
            ;; let's assume we added all directories using ls
            (if (= name "..") ;; only one level up
              (recur lines** (zip/up zipper*) :command)
              (recur lines** (find-in-dir zipper* name) :command)))

          :ls
          (let [[line & lines**] lines*]
            (condp starts-with? line
              "$"
              (recur lines* zipper* :command)
              "dir"
              (let [[_ name] (str/split line #" ")]
                (recur lines** (zip/append-child zipper* {:name name :size 0 :children []}) :ls))
              ;; assume we don't call ls twice in the same dir
              (let [[value] (str/split line #" ")
                    size (Integer/parseInt value)
                    node (zip/edit zipper* update :size (partial + size))]
                (recur lines** node :ls)))))))))

(defn ->size-tree
  [node]
  (let [size (:size node)
        children (mapv ->size-tree (:children node))]
    (if (empty? children)
      size
      (let [children-size (reduce
                            (fn [acc value]
                              (if (vector? value)
                                (+ acc (first value))
                                (+ acc value))) 0 children)]
        [(+ size children-size) children]))))

(defn calc-total-size
  [node]
  (if (vector? (first node))
    (calc-total-size (first node))
    (let [[size children] node
          size (if (> size 100000) 0 size)]
      (if (empty? children)
        size
        (+ size (calc-total-size children))))))

(defn sum-of-sizes [input]
  "По журналу сеанса работы в терминале воссоздаёт файловую систему
и подсчитывает сумму размеров директорий, занимающих на диске до
100000 байт (сумма размеров не учитывает случай, когда найденные
директории вложены друг в друга: размеры директорий всё так же
суммируются)."
  (let [tree (build-tree input)
        size-tree (->size-tree tree)]
    (calc-total-size size-tree)))
