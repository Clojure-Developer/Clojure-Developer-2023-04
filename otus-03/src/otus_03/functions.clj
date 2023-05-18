(ns otus-03.functions
  (:import (javax.sound.sampled AudioSystem AudioFormat$Encoding)))

;; безымянные функции

(fn [])

(fn [] (println "O hai"))

(fn [name] (println "Hello" name))

(fn [greeting name] (println greeting name))

((fn [greeting name] (println greeting name)) "Hello" "world")

;;(1 2 3)
;;("greeting" "world")


;; литерал функции

#(+ 1 %)

(#(println %1 %2) "Hey" "world")


;; поименованные функции

;;(def greeter (fn [greeting name] (println greeting name)))
(defn greeter [greeting name]
  (println greeting name))

(defn greeter*
  "An advanced version of a standard greeter."
  [greeting name]
  (println greeting name))

(defn- private [])

;; функции с переменным числом аргументов

(defn variadic [arg1 arg2 & rest]
  (println arg1 arg2 rest))

(variadic 'a 'b)

(variadic 'a 'b 'c 'd)

;;(fn [x y & zs] (println x y zs))
#(println %1 %2 %&)


;; мультиарные функции

(defn messenger
  "Prints welcome message."
  ([]    (messenger "Hello world!"))
  ([msg] (println msg)))

(messenger)

(messenger "Yay")


;; инварианты

(defn constrained-sqr [x]
  {:pre  [(pos? x)]
   :post [(> % 16), (< % 225)]}
  (* x x))

(constrained-sqr 5)

;;(constrained-sqr -1)

;;(constrained-sqr 1)


;; замыкания

(defn adder [x]
  (fn [y]
    (+ x y)))

(def add1 (adder 1))

(add1 42)


;; способы вызова

(apply list '(1 2 3 4))
(apply list 1 '(2 3 4))
(apply list 1 2 '(3 4))
(apply list 1 2 3 '(4))


(defn greet [name & rest]
  (apply println "Привет," name rest))

(greet "Андрей" "Сергей" "Алексей")


(defn multiplier [x]
  (partial * x))


(def mult10 (multiplier 10))

(mult10 42)

;; (defn non-negative-int? [x]
;;   (not (neg-int? x)))
(def non-negative-int? (complement neg-int?))

(non-negative-int? 5)
(non-negative-int? 0)


;; ->

;;(first (.split (.replace (.toUpperCase "a b c d") "A" "X") " "))
(-> "a b c d"
    .toUpperCase
    (.replace "A" "X")
    (.split " ")
    first)

;; https://4clojure.oxal.org/#/problem/38
(#(-> %&
      sort
      reverse
      first)
 1 8 3 4)

;; ->>

(defn repl []
  ;;(while true
  ;;  (println (eval (read))))
  (->> (read)
       eval
       println
       (while true)))


;; рекурсия

;; (defn factorial-naive [n]
;;   (if (= n 1)
;;     1
;;     (* n (recur (- n 1)))))

(defn factorial [n]
  (letfn [(factorial-inner [f n]
            (if (= n 1)
              f
              (recur (* f n) (- n 1))))]
    (factorial-inner 1 n)))

(factorial 10)


(declare my-odd?)

(defn my-even? [n]
  {:pre [(not (neg-int? n))]}
  (if (zero? n)
    true
    #(my-odd? (dec n))))

(defn my-odd? [n]
  {:pre [(not (neg-int? n))]}
  (if (zero? n)
    false
    #(my-even? (dec n))))

(trampoline my-even? 42)


;; цикл loop

(defn counter [s]
  {:pre [(string? s)]}
  (loop [n (Integer/parseInt s)]
    (println n)
    (when (pos-int? n)
      (recur (- n 1)))))

(counter "10")

(defn average
  "Calculates average value of given collection."
  [coll]
  (float (/ (reduce + coll)
            (count coll))))

(defn analyze-sound [filename]
  (let [mp3-file (new java.io.File filename)
        audio-in (AudioSystem/getAudioInputStream mp3-file)
        audio-decoded-in (AudioSystem/getAudioInputStream
                          AudioFormat$Encoding/PCM_SIGNED audio-in)
        buffer (make-array Byte/TYPE 4096)]
    (loop [result (list)]
      (let [size (.read audio-decoded-in buffer)]
        (if (pos-int? size)
          (recur (conj result (average buffer)))
          result)))))


;; Функции высшего порядка в стандартной библиотеке

;; reduce + range
(defn fibonacci-seq [n]
  (reduce
   (fn [a _]
     (concat a (list
                (+ (last a)
                   (last (butlast a))))))
   '(0 1)
   (range n)))

(defn factorial* [n])


;; memoize

(defn fibonacci-naive [n]
  (condp = n
    0 1
    1 1
    (+ (fibonacci-naive (- n 1))
       (fibonacci-naive (- n 2)))))

(time (fibonacci-naive 34))

(def fibonacci
  (memoize
   (fn [n]
     (condp = n
       0 1
       1 1
       (+ (fibonacci (- n 1))
          (fibonacci (- n 2)))))))

(time (fibonacci 34))


;; juxt
((juxt first count identity) "O hai")


;; fnil
(def incrementer (fnil inc 0))

(incrementer 1)
(incrementer nil)


;; iterate + partial
(def powers-of-two (iterate (partial *' 2) 2))

(defn my-partial [f & args]
  (fn [& inner-args]
    (apply f (concat args inner-args))))

(def powers-of-two* (iterate (my-partial *' 2) 2))


;; some-fn
(defn fizzbuzz [n]
  (map
   (some-fn #(and (zero? (mod % 3)) (zero? (mod % 5)) "FizzBuzz")
            #(and (zero? (mod % 3)) "Fizz")
            #(and (zero? (mod % 5)) "Buzz")
            str)
   (range 1 n)))

(println (fizzbuzz 17))

;; every-pred
(def odd-number? (every-pred number? odd?))

(odd-number? 3)
(odd-number? 4)
(odd-number? "")
