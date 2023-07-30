(ns otus-23.insta
  (:require [clojure.java.io :as io]
            [instaparse.core :as insta]
            [instaparse.transform :as insta.trsm]))



;; простой парсер
(def words-and-numbers
  (insta/parser
   "sentence = token (<whitespace> token)*
    <token> = word | number
    whitespace = #'\\s+'
    word = #'[a-zA-Z]+'
    number = #'[0-9]+'"))


(words-and-numbers "abc 123 def")




;; пример с трансформером
(def expr-parser
  (insta/parser
   "<S> = VAL | EXPR | PAR
    PAR = <'('> S <')'>
    <EXPR> = S OP S
    VAL = #'[0-9]+'
    OP = '+' | '-' | '*' | '/'"))


(expr-parser "((2*3)+1+2)/4")


(insta.trsm/transform
 {:PAR list
  :VAL #(Integer/parseInt %)
  :OP symbol}
 (expr-parser "1+(2+3)*(3/3)"))




;; базовый HTML парсер
(def html
  (insta/parser (io/resource "html.bnf")))


(html
 "<h1>Welcome to My Website</h1>")

(html
 (slurp (io/resource "sample.html")))

