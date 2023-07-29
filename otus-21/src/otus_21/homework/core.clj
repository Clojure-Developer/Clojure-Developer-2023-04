(ns otus-21.homework.core
  (:require [clojure.string :as s]
            [clojure.walk :as w]))


(defn parse-int [s]
  (Integer/parseInt s))

(defn parse-command
  "Парсит команду из строки. Возвращает nil, если это не удалось. Если
  удалось, возвращает команду и ее аргумент, если таковой имеется."
  [line]
  (let [parsed (re-matches #"[$][ ]+(cd|ls)(?:[ ]+(.*))?" line)]
    (rest parsed)))

(defn get-assoc-path
  "Возвращает путь в дереве каталогов для добавления элемента."
  [path name]
  (vec (mapcat (partial vector :children) (conj path name))))

(defn add-files
  "Добавляет в дерево файлы по указанному пути. Возвращает обновленный
  вариант дерева."
  [tree path files-and-dirs]
  (->> files-and-dirs
       ;; Каталоги игнорируются, в дерево они будут добавлять только
       ;; при переходе в них с помощью команды "cd".
       (filter (fn [[label _]]
                 (not= label "dir")))
       (reduce (fn [tree [file-size file-name]]
                 (assoc-in tree (get-assoc-path path file-name)
                           {:type :file :size (parse-int file-size)}))
               tree)))

(defn add-dir
  "Добавляет в дерево папку по указанному пути."
  [tree path dir-name]
   (assoc-in tree (get-assoc-path path dir-name)
             {:type :dir :size 0 :children {}}))

(defn parse-file-and-dirs
  "Получает список каталогов и файлов до следующей команды."
  [lines]
  (->> lines
       ;; Берем все строки, пока не встретим команду.
       (take-while #(empty? (parse-command %)))
       (map #(s/split % #" "))))

(defn parse-tree
  "Восстанавливает дерево каталог по истории команд. Возвращает дерево следующего
  вида:

  ```clojure
  {\"file1.txt\" {:type :file :size 100}
   \"file2.txt\" {:type :file :size 200}
   \"folder1\"
   {:type :dir
    :size nil
    :children
    {\"file3.txt\" {:type file :size 300}
     \"folder2\"
     {:type :dir
      :size nil
      :children {}}}}}
  ```
  "
  [data]
  (loop [lines (s/split-lines data) path [] tree {:type :dir :size nil :children {}}]
    (if (empty? lines) tree
      (let [[command dir] (parse-command (first lines))]
        (cond
          ;; Для команды "ls" добавляем файлы из текущего каталога
          ;; в дерево и переходим к следующей команде.
          (= command "ls")
          (let [files-and-dirs (parse-file-and-dirs (rest lines))]
            (recur (drop (inc (count files-and-dirs)) lines)
                   path
                   (add-files tree path files-and-dirs)))
          (= command "cd")
          (cond
            ;; Переход на уровень выше, убираем последний элемент из текущего пути.
            (= dir "..")
            (recur (rest lines)
                   (pop path)
                   tree)
            ;; Переход в корень, очищаем текущий путь.
            (= dir "/")
            (recur (rest lines)
                   []
                   tree)
            ;; Переход куда-то еще. Добавляем каталог к текущему пути, добавляем каталог
            ;; в дерево.
            :else
            (recur (rest lines)
                   (conj path dir)
                   (add-dir tree path dir))))))))

(defn update-dir-sizes
  "Добавляет в дерево размеры каталогов."
  [tree]
  (w/postwalk
    (fn [t] (if (= (:type t) :dir)
              ;; Если встретили каталог при обходе, считаем его размер как сумму
              ;; размеров дочерних файлов и каталогов.
              (assoc t :size (reduce + (map (comp :size val)
                                            (:children t))))
              t))
    tree))

(defn sum-of-sizes
  "По журналу сеанса работы в терминале воссоздаёт файловую систему
  и подсчитывает сумму размеров директорий, занимающих на диске до
  100000 байт (сумма размеров не учитывает случай, когда найденные
  директории вложены друг в друга: размеры директорий всё так же
  суммируются)."
  [data & {:keys [size-limit] :or {size-limit 100000}}]
  (->> data
       ;; Парсим команды в структуру каталогов.
       parse-tree
       ;; Считаем размеры каталогов.
       update-dir-sizes
       ;; Преобразуем дерево в последовательность нод.
       (tree-seq
         #(= (:type %) :dir)
         (comp vals :children))
       ;; Выбираем каталоги меньше определенного размера.
       (filter (fn [node]
                 (and (= (:type node) :dir)
                      (< (:size node) size-limit))))
       (map :size)
       (reduce +)))
