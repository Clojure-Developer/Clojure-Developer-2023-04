(ns otus-23.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]))



;; вспомогательные функции, детали реализации
(defn -return [value tail]
  {:value value :tail tail})

(comment
 (-return "hello" "world"))



(def -value :value)
(def -tail :tail)
(def -valid boolean)

(comment
 (-value (-return "hello" "world"))
 (-tail (-return "hello" "world"))

 (-valid (-return "hello" "world"))
 (-valid nil))






;; функции для визуализации и дебага
(defn -show [result]
  (if (-valid result)
    (str "-> " (pr-str (-value result)) " | " (pr-str (apply str (-tail result))))
    "!"))


(defn -tabulate [parser & inputs]
  (run! (fn [input]
          (printf "    %-10s %s\n" (pr-str input) (-show (parser input))))
        inputs))

(comment
 (println (-show (-return "hello" "world")))
 (-tabulate (constantly (-return "hello" "world")) "a" "b" "x" ""))







;; парсеры
(defn _empty
  "возвращает константное значение value"
  [value]
  (fn [input]
    (-return value input)))

(comment
 (-tabulate (_empty "hello") "a" "b" "x" ""))


(defn _char
  "возвращает первый символ из input если он удовлетворяет предикату"
  [pred]
  (fn [[first-char & rest-chars]]
    (when (and first-char (pred first-char))
      (-return first-char rest-chars))))

(comment
 (-tabulate (_char #{\a \b \c}) "a" "abc" "b" "x" ""))


(defn _map
  "трансформирует значение value, применяя функцию f"
  [f result]
  (when (-valid result)
    (-return (f (-value result)) (-tail result))))

(comment
 (-tabulate (comp (partial _map string/upper-case) (_char #{\a \b \c}))
            "a" "abc" "b" "x" ""))


(defn _combine
  "объединяет результаты работы двух парсеров (если оба отработали успешно)
   функция f применятся для конкатенации резальтатов"
  [f parser1 parser2]
  (fn [input]
    (let [result (parser1 input)]
      (when (-valid result)
        (_map (partial f (-value result))
              ((force parser2) (-tail result)))))))

(comment
 (-tabulate
  (_combine str
            (_char #{\a \b \c})
            (_char #{\x \y \z}))
  "ax" "abc" "bzzz" "x" ""))


(defn _either
  "возвращает результат применения первого парсера если парсер сматчился
   или результат второго парсера в противном случае"
  [parser1 parser2]
  (fn [input]
    (let [result (parser1 input)]
      (if (-valid result)
        result
        ((force parser2) input)))))

(comment
 (-tabulate
  (_either
   (_char #{\a \b \c})
   (_char #{\x \y \z}))
  "ax" "abc" "bzzz" "x" "zaa"))


(defn _parser [p]
  ;; добавляем парсер для терминального символа (его результат просто игнорируем)
  (let [p' (_combine
            (fn [result _] result) p (_char #{\u0000}))]
    (fn [input]
      ;; добавляем терминальный символ в конец строки
      (-value (p' (str input \u0000))))))

(comment
 (mapv
  (_parser
   (_combine str
             (_char #{\a \b \c})
             (_char #{\x \y \z})))
  ["ax" "abc" "bzzz" "x" "" "cy"]))





;; комбинаторы
(defn +char
  "проверяет находится ли символ в множестве символов chars"
  [chars]
  (_char (set chars)))

(comment
 (mapv
  (_parser
   (_combine str
             (+char "abc")
             (+char "xyz")))
  ["ax" "abc" "bzzz" "x" "" "cy"]))


(defn +char-not
  "проверяет НЕ находится ли символ в множестве символов chars"
  [chars]
  (_char (comp not (set chars))))

(comment
 (mapv
  (_parser
   (_combine str
             (+char-not "ax")
             (+char "xyz")))
  ["ax" "zx" "zz" "xy" "" "cy"]))


(defn +map
  "трансформирует значение в результате работы парсера"
  [f parser]
  (comp (partial _map f) parser))

(comment
 (mapv
  (_parser
   (+map string/upper-case
         (_combine str
                   (+char-not "ax")
                   (+char "xyz"))))
  ["ax" "zx" "zz" "xy" "" "cy"]))


(def +parser _parser)


(defn +ignore
  "заменяет игнорируемое значение на символ ignore"
  [parser]
  (+map (constantly 'ignore) parser))

(comment
 (-tabulate
  (+ignore (+map string/upper-case
                 (_combine str
                           (+char-not "ax")
                           (+char "xyz"))))
  "ax" "zx" "zz" "xy" "" "cy"))


(defn iconj
  "хелпер функция которая пропускает символы ignore"
  [coll value]
  (if (= value 'ignore)
    coll
    (conj coll value)))


(defn +seq
  "делаем последовательно применяемую коллекцию парсеров"
  [& parsers]
  (reduce (partial _combine iconj)                          ;; объединяем парсеры по очереди
          (_empty [])                                       ;; начальное состояние
          parsers))

(comment
 (-tabulate
  (+seq
   (+char-not "ax")
   (+char "xyz"))
  "ax" "zx" "zz" "xy" "" "cy"))


(defn +seqf
  "аналогично комбинатору +seq, но дополнительно принимает функцию f для аггрегации результатов парсеров"
  [f & parsers]
  (+map (partial apply f) (apply +seq parsers)))

(comment
 (-tabulate
  (+seqf str
         (+char-not "ax")
         (+char "xyz"))
  "ax" "zx" "zz" "xy" "" "cy")

 (-tabulate
  (+seqf str
         (+ignore (+char-not "ax"))
         (+char "xyz"))
  "ax" "zx" "zz" "xy" "" "cy"))


(defn +seqn
  "аналогично комбинатору +seq, но в качестве результата выбирается n-й элемент"
  [n & parsers]
  (apply +seqf #(nth %& n) parsers))

(comment
 (-tabulate
  (+seqn 0
         (+char-not "ax")
         (+char "xyz"))
  "ax" "zx" "zz" "xy" "" "cy"))


(defn +or
  "делаем последовательно применяемую коллекцию парсеров,
   каждый последующий применяется если предыдущий парсер не сматчился
   продолжаем применять парсеры пока один из них не сматчится"
  [parser & parsers]
  (reduce _either parser parsers))

(comment
 (-tabulate
  (+or
   (+char "AX")
   (+char "xyz"))
  "ax" "zx" "zz" "A" "xy" "" "X" "cy"))


(defn +opt
  "создаёт опциональный парсер. если нет матча, не считаем ошибкой и идём дальше"
  [parser]
  (+or parser (_empty nil)))

(comment
 (-tabulate
  (+opt
   (+char "xyz"))
  "ax" "zx" "zz" "A" "xy" "" "X" "cy"))


(defn +star
  "применяет парсер ноль или несколько раз, пока парсер возращает результат"
  [parser]
  (+or (+seqf cons parser (delay (+star parser))) (_empty ())))

(comment
 (-tabulate
  (+star
   (+char "xyz"))
  "ax" "zx" "zz" "A" "xy" "" "X" "cy"))


(defn +plus
  "применяет парсер один или несколько раз, пока парсер возращает результат"
  [parser]
  (+seqf cons parser (+star parser)))

(comment
 (-tabulate
  (+plus
   (+char "xyz"))
  "ax" "zxyy123" "zzi" "A" "xy" "" "X" "cy"))


(defn +str
  "конкатенирует несколько значений (если парсер возвращает список) в строку"
  [parser]
  (+map (partial apply str) parser))

(comment
 (-tabulate
  (+str
   (+plus
    (+char "xyz")))
  "ax" "zxyy123" "zzi" "A" "xy" "" "X" "cy"))









;; JSON парсер
(def *digit
  (+char "0123456789"))


(defn sign [s prime [_dot decimal]]
  (let [num-seq (if (seq decimal)
                  (concat prime ["."] decimal)
                  prime)]
    (if (#{\- \+} s)
      (cons s num-seq)
      num-seq)))


;; парсер для чисел с учётом знака
(def *number
  (->> (+seqf sign (+opt (+char "-+")) (+plus *digit) (+opt (+seq (+char ".") (+plus *digit))))
       (+str)
       (+map read-string)))

(comment
 (-tabulate
  *number
  "1" "123" "123ee" "-12" "+33" "-hello" "--12" "0.55"))


;; парсер для строк (всё что между ковычек)
(def *string
  (+seqn 1
         (+char "\"")
         (+str (+star (+char-not "\"")))
         (+char "\"")))

(comment
 (-tabulate
  *string
  "\"hello\"" "\"hello" "\"\""))


;; парсер пробельных символов
(def *space
  (+char " \t\n\r"))


;; парсер нескольких пробельных символов (которые всегда игнорируются)
(def *ws
  (+ignore (+star *space)))

(comment
 (-tabulate
  (+seq *ws *string *ws)
  "  \"hello\" " "\"hello" "\"\""))


;; парсер для значения null
(def *null
  (+map (constantly 'null) (+seq (+char "n") (+char "u") (+char "l") (+char "l"))))

(comment
 (-tabulate
  *null
  "null" "null123" ""))


(def *letter
  (+char "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"))

;; парсер идентификаторов (ключи в объектах), могут содержать буквы числа и нижнее подчёркивание
(def *identifier
  (+str (+seqf cons *letter (+star (+or *letter *digit (+char "_"))))))

(comment
 (-tabulate
  *identifier
  "hello" "hello-world" "" "123"))



(defn *seq [begin parser end]
  (+seqn 1
         (+char begin)
         *ws
         (+opt (+seqf cons parser (+star (+seqn 1 *ws (+char ",") *ws parser))))
         *ws
         (+char end)))

;; парсер для JSON массивов
(defn *array [parser]
  (+map vec (*seq "[" parser "]")))

(comment
 (-tabulate
  (*array *number)
  "[]" "[1, 2]" "[123]" "[ 1,2,3   ,4]" "" ","))


(defn *member [parser]
  (+seq (+ignore (+opt (+char "\"")))
        *identifier
        (+ignore (+opt (+char "\"")))
        *ws
        (+ignore (+char ":"))
        *ws
        parser))

;; парсер для JSON объектов
(defn *object [parser]
  (+map #(into {} %)
        (*seq "{" (*member parser) "}")))

(comment
 (-tabulate
  (*object *number)
  "{}" "{a: 1}" "{a: 1, b : 2}" "{hello: 1}"))


;; парсер всех возможных значений в JSON
;; массивы и объекты используют рекурсивный вызов парсера
(defn *value []
  (+or *number
       *null
       *string
       (*array (delay (*value)))
       (*object (delay (*value)))))

(comment
 (-tabulate
  (*value)
  "{}" "{a: 1}" "[1, 3, [2], {c: 123} ]" "123" "\"hello\""))


;; создаём JSON парсер
(def json
  (+parser (+seqn 0 *ws (*value) *ws)))

(comment
 (json "[1, 3, [2], {c: 123} ]")

 (json (slurp (io/resource "sample.json"))))
