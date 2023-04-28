(ns otus.syntax)


;; I'm a list
()

;; function call
(+ 1 2)

;; just a list
'(+ 1 2)



;; Python
;; print('hello clojurians')
(print "hello clojurians")

(println "hello clojurians")



;; nested expressions
(+ (- 12 4)
   (* 3 6))



;; I'm a comment (+ 1 2)

#_"I'm a comment as well" (+ 1 2)

(+ (- 12 4)
   #_(/ 16 4)
   (* 3 6))

(comment
 (println "comment again")
 (+ 12 3))



;; docs
(clojure.repl/doc +)

(clojure.repl/dir clojure.core)

(clojure.repl/find-doc "split")

(clojure.repl/source clojure.string/split)
