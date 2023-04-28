(ns otus.data-types)


;; numbers
43
-13
1.4
22/7
15N

(type 12)
(type 15N)
(type 15M)
(type 22/7)

(bigdec 123)

(+ 34 22.5)

(int 22.5)
(double 22)



;; strings
"hello"
\e
#"[0-9]+"

"hello
 qweqwe
 qweqwe"

(type #"[0-9]+")

(= \e "e")

(str "hello"
     " "
     "world")

(format "hello %s" "Clojure")



;; symbols
'+
'map
'clojure.string/split

(symbol "qweqwe")

(name 'qweqwe)
(str 'ns/qweqwe)
(name 'ns/qweqwe)

nil
true false

(type :alpha)

:release/alpha

:alpha



;; collections
'(1 2 3)

(type '(1 2 3))
(type [1 2 3])


(first '(1 2 3))
(last [1 2 3])

(get [1 2 3 4] 3)

(conj '(1 2 3) 4)
(conj [1 2 3] 4)

(list 1 2 3) ;; '()
(vector 1 2 3) ;; []

(count [1 2 3])



#{1 2 3}

#{1 2 3 1} ;; will cause error

(set [1 2 3 1])

(conj #{1 2 3} 1)

;; elements could be anything
["qwe" 2 false :qwe [1 2 3] #{1 2 3}]
#{1 "qwe" :qwe}

;; equality done by the content of collections
(= [1 2] [1 2])



{:foo  1
 :bar  "2"
 "baz" 3
 1     nil
 12    22}

(hash-map :foo 1 :bar 2)

(assoc {:foo 1 :bar 2} :baz 3)
(dissoc {:foo 1, :bar 2, :baz 3} :baz)

(get {:foo 1 :bar 2} :foo)
