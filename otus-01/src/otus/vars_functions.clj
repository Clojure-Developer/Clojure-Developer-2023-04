(ns otus.vars-functions)



;; global variables
(def course "Clojure Developer")

(def students-cont 8)

(def is-123123123-it-first-lesson? true)

(def !valid_$$_*&_name? true)



(def course "This is a course name (I'm a documentation string)"
  "Clojure Developer")


;; local variables
(let [x 1
      y 2]
  (+ x y))


;; functions
(fn [x y]
  (* x y))


(def multiply
  (fn [x y]
    (* x y)))


(defn multiply [x y]
  (* x y))


(multiply 2 3)


(defn multiply
  "Multiply two numbers"
  [x y]
  (* x y))


(defn square [x]
  (* x x))


(defn sum-of-squares [x y]
  (let [x-square (square x)
        y-square (square y)]
    (+ x-square y-square)))


(sum-of-squares 4 5)


;; private function inside the namespace
(defn- don't-touch-me [x]
  (+ x 2))
