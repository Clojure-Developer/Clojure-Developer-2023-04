(ns otus-25.dsl)

"""
Figure 1. Truth tables for some logical gates

NOT GATE          AND GATE           DMUX GATE
========          ========           =========

IN || OUT        A B || OUT        IN SEL || A B
---------        ----------        -------------
0  ||   1        0 0 ||  0          0  0  || 0 0
1  ||   0        0 1 ||  0          0  1  || 0 0
                 1 0 ||  0          1  0  || 1 0
                 1 1 ||  1          1  1  || 0 1
"""

"""
Figure 2. Inside of a DMUX

IN SEL || NSEL=(NOT SEL) A=(AND IN NSEL) B=(AND IN SEL) || A B
--------------------------------------------------------------
 0   0 ||   1            0               0              || 0 0
 0   1 ||   0            0               0              || 0 0
 1   0 ||   1            1               0              || 1 0
 1   1 ||   0            0               1              || 0 1
"""

;; Реализация через функции

;; Штрих Шефера, отрицание конъюнкции, и-не

(defn nand* [a b]
  (let [out (if (= 2 (+ a b))
              [0]
              [1])]
    out))
 
;; Штрих Шеффера образует базис для пространства булевых функций от двух переменных.
;; Используя только штрих Шеффера, можно построить все остальные операции.

(defn not* [in]
  (let [out (nand* in in)]
    out))

(defn and* [a b]
  (let [[w] (nand* a b)
        out (not* w)]
    out))

(defn dmux* [in sel]
  (let [[nsel] (not* sel)
        [a] (and* in nsel)
        [b] (and* in sel)
        out [a b]]
    out))

;; Как мы бы хотели описывать логические схемы?

'(defgate not* [in] => [out]
   (nand* [in in] => [out]))

'(defgate and* [a b] => [out]
   (nand* [a b] => [w])
   (not* [w] => [out]))

'(defgate dmux* [in sel] => [a b]
   (not* [sel] => [nsel])
   (and* [in nsel] => [a])
   (and* [in sel] => [b]))

(comment
  ;; Вариант без лишних скобок

  '(defgate and* a b => out
     (nand* a b => w)
     (not* w => out))
  )

;; Необходимо написать макрос, который бы осуществлял переход из
'(defgate dmux* [in sel] => [a b]
   (not* [sel] => [nsel])
   (and* [in nsel] => [a])
   (and* [in sel] => [b]))

;; в
'(let [[nsel] (not* sel)
       [a] (and* in nsel)
       [b] (and* in sel)
       out [a b]]
   out)

;; а ещё удобнее для реализации в
'(let [[nsel] (not* sel)]
   (let [[a] (and* in nsel)]
     (let [[b] (and* in sel)]
       [a b])))

;;
;; Напишем макрос и вспомогательную функцию к нему.
;;

(defn expand-defgate
  "recursively expands a gate with the right scoping"
  [forms outs]
  (if (seq forms)
    (let [[form & rest] forms
          [local-gate local-ins _=> local-outs] form]
      `(let [~(vec local-outs) (~local-gate ~@local-ins)]
         ~(expand-defgate rest outs)))
    outs))

(defmacro defgate
  "defines a logical gate with a name, input/output
   pins, and a implementation consisting of other gates"
  [gate ins _=> outs & forms]
  `(defn ~gate ~ins
     ~(expand-defgate forms outs)))

#_:clj-kondo/ignore
(comment

  (defn nand* [a b]
    (let [out (if (= 2 (+ a b))
                [0]
                [1])]
      out))

  (defgate not* [in] => [out]
    (nand* [in in] => [out]))

  (defgate and* [a b] => [out]
    (nand* [a b] => [w])
    (not* [w] => [out]))

  (defgate dmux* [in sel] => [a b]
    (not* [sel] => [nsel])
    (and* [in nsel] => [a])
    (and* [in sel] => [b]))

  (defgate dmux* [in sel] => [a b]
    (not* [sel] => [nsel])
    (and* [in nsel] => [a])
    (and* [in sel] => [b]))

  (macroexpand-1 '(defgate dmux* [in sel] => [a b]
                    (not* [sel] => [nsel])
                    (and* [in nsel] => [a])
                    (and* [in sel] => [b])))

  '(clojure.core/defn dmux* [in sel]
     (clojure.core/let [[nsel] (not* sel)]
       (clojure.core/let [[a] (and* in nsel)]
         (clojure.core/let [[b] (and* in sel)]
           [a b]))))
  )
