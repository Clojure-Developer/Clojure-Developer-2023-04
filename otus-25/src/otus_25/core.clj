(ns otus-25.core
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(defmacro foreach
  [[sym coll] & body]
  `(loop [coll# ~coll]
     (when-let [[~sym & xs#] (seq coll#)]
       ~@body
       (recur xs#))))

#_:clj-kondo/ignore
(foreach [x [1 2 3]]
         (println x))


(comment
  (defn reverse-it-helper
    [form]
    (walk/postwalk (fn [x]
                     (if (symbol? x)
                       (symbol (string/reverse (name x)))
                       x))
                   form))

  (defmacro reverse-it
    [form]
    (reverse-it-helper form))

  (defmacro reverse-it
      [form]
      (println :form form)
      (let [result (reverse-it-helper form)]
        (println :result result)
        result))
  
  #_:clj-kondo/ignore
  (reverse-it
   (qesod [gra (egnar 5)]
          (nltnirp (cni gra))))

  (macroexpand-1 '(reverse-it
                   (qesod [gra (egnar 5)]
                          (nltnirp (cni gra)))))
  )

;;
;; Отладка макросов
;;

(defn oops [arg]
  (frobnicate arg))

(defmacro oops [arg]
  (list 'frobnicate arg))

;; (def frobnicate identity)

(oops 123)

(macroexpand-1 '(oops 123))

;; Единоразовое раскрытие макроса (не по отношению ко вложенным):

(macroexpand-1 '(reverse-it
                 (qesod [gra (egnar 5)]
                        (nltnirp (cni gra)))))

;; Полное раскрытие макроса (не по отношению ко вложенным):

(pprint
 (macroexpand '(reverse-it
                (qesod [gra (egnar 5)]
                       (nltnirp (cni gra))))))

(macroexpand '(cond a b c d))

;; Полное раскрытие в том числе вложенных макросов:

(walk/macroexpand-all '(cond a b c d e f))

;; macroexpand-all - это ИМИТАЦИЯ полного развертывания макросов,
;; выполняемого компилятором. Специальные формы обрабатываются некорректно.

(walk/macroexpand-all ''(when x a))

;; должно было развернуться в (quote (when x a))

;;
;; Синтаксис
;;

(defmacro hello
  [name]
  (list 'println name))

(macroexpand '(hello "Brian"))

;; Запись макроса через функции для работы со списками:

(defmacro while
  [test & body]
  (list 'loop []
        (concat (list 'when test)
                body
                '((recur)))))

(macroexpand '(while (= 1 2)
                (println :heh)
                (println :lol)))

;; Запись макросы при помощи специализированных форм:

(defmacro while
  [test & body]
  `(loop []
     (when ~test
       ~@body
       (recur))))

;;
;; quote vs syntax-quote
;;

(def foo 123)

[foo (quote foo) 'foo `foo]

(require '[clojure.walk :as walk])

`map
`walk/prewalk
`prewalk

;;
;; unquote и unquote-splicing
;;

;; unquote

(def foo 123)

(list `map `println [foo])
;; => (clojure.core/map clojure.core/println [123])

`(map println [~foo])
;; => (clojure.core/map clojure.core/println [123])

`(map println ~[foo])
;; => (clojure.core/map clojure.core/println [123])


;; Размаскирование списка или вектора размаскирует форму целиком,
;; таким образом мы можем вызывать функции и добавлять их результат в замаскированную форму.

`(println ~(keyword (str foo)))

;; unquote-splicing

(let [defs '((def x 123)
             (def y 456))]
  (concat (list 'do)
          defs))
;; => (do (def x 123) (def y 456))

(let [defs '((def x 123)
             (def y 456))]
  `(do ~@defs))
;; => (do (def x 123) (def y 456))

;; Полезно для внедрения "тела" макроса в замаскированну форму

(defmacro foo
  [& body]
  `(do-something ~@body))

(macroexpand-1 '(foo (doseq [x (range 5)]
                       (println x))
                     :done))

'(otus-25.core/do-something
  (doseq [x (range 5)] (println x))
  :done)

;; Магии нет :(

'`(map println ~[foo])

'(clojure.core/seq
  (clojure.core/concat
   (clojure.core/list 'clojure.core/map)
   (clojure.core/list 'clojure.core/println)
   (clojure.core/list [foo])))

;;
;; Когда использовать макрсоы?
;;

(defn fn-hello
  [x]
  (str "Hello, " x "!"))

(defmacro macro-hello
  [x]
  `(str "Hello, " ~x "!"))

(fn-hello "Brian")
;; => "Hello, Brian!"

(macro-hello "Brian")
;; => "Hello, Brian!"

(map fn-hello ["Brian" "Not Brian"])
;; => ("Hello, Brian!" "Hello, Not Brian!")

(map macro-hello ["Brian" "Not Brian"])
; Syntax error compiling at (src/otus_25/core.clj:202:1).
; Can't take value of a macro: #'otus-25.core/macro-hello

(map #(macro-hello %) ["Brian" "Not Brian"])
;; => ("Hello, Brian!" "Hello, Not Brian!")

;;
;; Гигиена
;;

(defmacro unhygienic
  [& body]
  `(let [x :oops]
     ~@body))

(unhygienic (println "x:" x))
; Syntax error macroexpanding clojure.core/let at (src/otus_25/core.clj:220:1).

;; Форма let требует, чтобы символ, к которому осуществляется привязка значения,
;; был простым.

(macroexpand-1 `(unhygienic (println "x:" x)))

'(clojure.core/let [otus-25.core/x :oops]
   (clojure.core/println "x:" otus-25.core/x))

(defmacro still-unhygienic
  [& body]
  `(let [~'x :oops]
     ~@body))

(still-unhygienic (println "x:" x))
; x: :oops
;; => nil

(macroexpand-1 '(still-unhygienic
                 (println "x:" x)))

'(clojure.core/let [x :oops]
   (println "x:" x))

(let [x :this-is-important]
  (still-unhygienic
   (println "x:" x)))
; x: :oops
;; => nil

;; Генераторы символов во спасение

(gensym)

(gensym "sym")

(defmacro hygienic
  [& body]
  (let [sym (gensym)]
    `(let [~sym :macro-value]
       ~@body)))

(let [x :important-value]
  (hygienic
   (println "x:" x)))
; x: :important-value
;; => nil

;; Автоматическая генерация символов внутри syntax-quote при помощи постфикса #

(defmacro hygienic
  [& body]
  `(let [x# :macro-value]
     ~@body))

;; В пределах единственной формы синтаксического маскирования,
;; все вхождения указанного динамически генерируемого символа будут
;; преобразовываться в один и тот же фактический символ:

`(x# x#)

(defmacro auto-gensyms
  [& numbers]
  `(let [x# (rand-int 10)]
     (+ x# ~@numbers)))

(auto-gensyms 1 2 3 4 5)

(macroexpand-1 '(auto-gensyms 1 2 3 4 5))

'(clojure.core/let [x__8265__auto__ (clojure.core/rand-int 10)]
   (clojure.core/+ x__8265__auto__ 1 2 3 4 5))

;; Фактические символы будут различаться, потому что использовано две формы
;; syntax-quote:

[`x# `x#]

(defmacro our-doto
  [expr & forms]
  `(let [obj# ~expr]
     ~@(map (fn [[f & args]]
              `(~f obj# ~@args))
            forms)
     obj#))

(macroexpand-1 '(our-doto "It works"
                          (println "I can't believe it")))

'(clojure.core/let [obj__8599__auto__ "It works"]
   (println obj__8598__auto__ "I can't believe it")
   obj__8599__auto__)

(our-doto "It works"
          (println "I can't believe it"))


(defmacro our-doto
  [expr & forms]
  (let [obj (gensym "obj")]
    `(let [~obj ~expr]
       ~@(map (fn [[f & args]]
                `(~f ~obj ~@args))
              forms)
       ~obj)))

(our-doto "It works"
          (println "WOW"))

;;
;; Предоставление пользователю права выбора имен
;;

(defmacro with
  [name & body]
  `(let [~name 5]
     ~@body))

(with bar (+ 10 bar)) ;; => 15
(with foo (+ 40 foo)) ;; => 45

;;
;; Двукратное вычисление
;;

(defmacro spy
  [x]
  `(do
     (println "spied" '~x ~x)
     ~x))

(spy 2)
; spied 2 2
;; => 2

(spy (rand-int 10))
; spied (rand-int 10) 9
;; => 7

(macroexpand-1 '(spy (rand-int 10)))

'(do (clojure.core/println "spied" '(rand-int 10) (rand-int 10))
     (rand-int 10))

(defmacro spy
  [x]
  `(let [x# ~x]
     (println "spied" '~x x#)
     x#))

(macroexpand-1 '(spy (rand-int 10)))

'(clojure.core/let
  [x__8465__auto__ (rand-int 10)]
   (clojure.core/println "spied" '(rand-int 10) x__8465__auto__)
   x__8465__auto__)

(spy (rand-int 10))
; spied (rand-int 10) 2
;; => 2

;; Двукратное вычисление, даже если его можно обойти, – это дурно пахнущий код.
;; Это повод задуматься об использовании вспомогательной функции.

(defn spy-helper [expr value]
  (println expr value)
  value)

(defmacro spy
  [x]
  `(spy-helper '~x ~x))

;; Пишем свой thread-first макрос

(comment
  (defn ensure-seq
    [x]
    (if (seq? x)
      x
      (list x)))

  (defn insert-second
    "Вставляет x на место второго элемента в последовательность y."
    [x ys]
    (let [ys (ensure-seq ys)]
      #_(list* (first ys) x (rest ys))
      `(~(first ys) ~x ~@(rest ys))))

  (defmacro =>
    "Вставляет x в последующие формы."
    ([x]
     x)
    ([x form]
     (insert-second x form))
    ([x form & more]
     `(=> (=> ~x ~form) ~@more)))

  (=> [1 2 3]
      (conj 4)
      reverse
      println)
; (4 3 2 1)
;; => nil
  )

;;
;; Неявные аргументы: &env и &form
;;

(defmacro spy-env []
  (let [ks (keys &env)]
    `(println (zipmap '~ks [~@ks]))))

((fn [a]
  (let [b 1]
    (let [c 2
          d 3]
      (spy-env)
      (+ a b c d))))
 4)

(defmacro simplify
  [expr]
  (let [locals (set (keys &env))]
    (if (some locals (flatten expr))
      expr
      (do
        (println "Precomputing: " expr)
        (list `quote (eval expr))))))

(defn f
  [a b c]
  (+ a b c (simplify (apply + (range 5e7)))))

(time (f 1 2 3))

(defn f' [a b c]
  (simplify (apply + a b c (range 5e7))))

(time (f' 1 2 3))

;; Макрос - это функция с метаданными :macro.
;; Первые два аргумента макрофункции - form и env,
;; которые неявным образом прокидываются компилятором.
;; Эти аргументы можно передать в явном виде при вызове вара макроса.

(@#'simplify '(simplify (inc 1)) {} '(inc 1))

(@#'simplify '(simplify (inc 1)) {'x nil} '(inc x))

;; &form

(defmacro ontology
  [& triples]
  (every? (fn [x]
            (or (== 3 (count x))
                (throw (IllegalArgumentException.
                        (format
                         "`%s` provided to `%s` on line %s has < 3 elements"
                         x
                         (first &form)
                         (-> &form meta :line))))))
          triples)
  ;; some work
  )

(ontology ["Boston" :capital-of])


;; Тестирование контекстных макросов

(defn macroexpand-1-env
  [env form]
  (if-let [[x & xs] (and (seq? form)
                         (seq form))]
    (if-let [v (and (symbol? x)
                    (resolve x))]
      (if (-> v meta :macro)
        (apply @v form env xs)
        form)
      form)
    form))

(macroexpand-1-env '{} '(simplify (range 10)))

(macroexpand-1-env '{range nil} '(simplify (range 10)))

;; Ряд форм core библиотеки, реализованных как макросы.

(comment
  or
  doseq
  for
  while
  defn
  defmacro
  )
