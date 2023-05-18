(ns otus-02.strings
  (:require [clojure.string :as string])
  (:import [java.time LocalDateTime]))


;; functions from clojure.core
(type "hello")
(type \c)

(not= "c" \c)

(count "hello")

(empty? "0123")
(empty? "")


(str 123)

(.toString 123)

(str 123 " - hello " {:a 1})

(string? 123)
(string? "123")

(get "123" 1)




(defn get-price [n]
  (let [r (-> (rem n 1) (* 100) int)
        n (int n)]
    (str "$" n "," r)))

(get-price 1.26)


(format "$ %.2f" 1.259345353)
(format "Local time: %tT" (LocalDateTime/now))



(subs "hello dude" 6)
(subs "hello dude" 0 4)

(subs "hello dude" 0 14)




(apply str (reverse "hello dude"))

(apply str (reverse "hello dude"))


(int \a)
(char 104)
(map int "hello dude")

(->> "hello dude"
     (map (comp char inc int))
     (apply str))



;; functions from clojure.string
(string/starts-with? "hello dude" "hell")
(string/ends-with? "hello dude" "dude")


(string/join " | "  [1 2 3 4])
(string/join [1 2 3 4])

(string/split "1 , 2 , 3 , 4"  #" , ")


(string/reverse "hello dude")

(string/lower-case "fOo")
(string/upper-case "fOo")
(string/capitalize "fOo")


(string/escape "foo|bar|quux" {\| "||"})
