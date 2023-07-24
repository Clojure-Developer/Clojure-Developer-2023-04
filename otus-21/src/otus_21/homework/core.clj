(ns otus-21.homework.core
  (:require [clojure.string :as str]
            [clojure.zip :as z]))

(defn find-by-name-among-sublings
  [loc name]
  (if (= (:name (z/node loc)) name)
    loc
    (if-let [next (z/right loc)]
      (find-by-name-among-sublings next name))))

(defn apply-cd-command
  [loc cmd-arg]
  (if (= ".." cmd-arg)
    (z/up loc)
    (find-by-name-among-sublings (z/down loc) cmd-arg)))

(defn append-dir
  [loc name]
  (z/append-child loc {:name name :children []}))

(defn append-file
  [loc name size]
  (z/append-child loc {:name name :size size}))

(defn apply-ls-command
  [loc ls-output]
  (reduce
    (fn [loc line]
      (let [[x y] (str/split line #" ")]
        (if (= "dir" x)
          (append-dir loc y)
          (append-file loc y (parse-long x)))))
    loc
    ls-output))

(defn output-cmd-line?
  [cmd-line]
  (not= (get cmd-line 0) \$))

(defn take-out-cmd-lines
  [cmd-lines]
  [(take-while output-cmd-line? cmd-lines)
   (drop-while output-cmd-line? cmd-lines)])

(defn make-dir-zipper
  []
  (z/zipper
    (fn [val]
      (or (vector? val) (and (map? val) (contains? val :children))))
    (fn [val]
      (if (vector? val)
        (seq val)
        (:children val)))
    (fn [node children]
      (if (map? node)
        (assoc node :children (vec children))
        (vec children)))
    [{:name "/" :children []}]))

(defn build-tree
  [input]
  (let [zipper (make-dir-zipper)
        lines (str/split-lines input)]
    (z/root
      (loop [loc zipper
             [cmd & cmd-lines] lines]
        (if (some? cmd)
          (let [[_ y z] (str/split cmd #" ")]
            (cond
              (= "cd" y)
              (recur
                (apply-cd-command loc z)
                cmd-lines)

              (= "ls" y)
              (let [[out-cmd-lines rest-cmd-lines] (take-out-cmd-lines cmd-lines)]
                (recur
                  (apply-ls-command loc out-cmd-lines)
                  rest-cmd-lines))

              :else
              (recur zipper cmd-lines)))
          loc)))))

(def calc-sum-of-dir
  (memoize
    (fn [dir]
      (transduce
        (map
          (fn [item]
            (if (contains? item :size)
              (:size item)
              (calc-sum-of-dir item))))
        + (:children dir)))))

(defn get-dir-sizes
  [item]
  (when (contains? item :children)
    (let [size (calc-sum-of-dir item)]
      (lazy-cat
        [size]
        (flatten (filter some? (map get-dir-sizes (:children item))))))))

(defn sum-of-sizes [input]
  "По журналу сеанса работы в терминале воссоздаёт файловую систему
и подсчитывает сумму размеров директорий, занимающих на диске до
100000 байт (сумма размеров не учитывает случай, когда найденные
директории вложены друг в друга: размеры директорий всё так же
суммируются)."
  (let [tree (build-tree input)]
    (transduce (filter #(< % 100000)) +
               (get-dir-sizes (first tree)))))
