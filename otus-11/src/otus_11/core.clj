(ns otus-11.core)

;; * Sequence abstraction

(first [1 2 3])
(first '(1 2 3))
(first {:a 1 :b 2})

(next [])
(rest (rest []))

;; * Ленивость

(defn geom [start mul stop]
  (lazy-seq
   (when (< start stop)
     (println (str "=> " start))
     (cons start
           (geom (* start mul) mul stop)))))

;; ленивые последовательности, выраженные рекурсивно,
;; не растят стек:

(comment
  (letfn [(ones [] (lazy-seq (cons 1 (ones))))]
    (reduce + (take 1000000 (ones))))
  ;; => 1000000
  )

;; * Бесконечные последовательности

(defn geom-inf [start mul]
  (lazy-seq
   (cons start
         (geom-inf (* start mul) mul))))

(def three-nums
  (lazy-cat
   '(1 2)
   (do (println "calculating third num")
       [3])))

(comment
  (print (take 2 three-nums)) ;; не напечатает сообщение
  (print three-nums) ;; напечатает
  (print three-nums) ;; не напечатает из-за кеширования
  )

;; * Трансдьюсеры
;; ** reducers

(defn conj-even
  ;; инициализация аккумулятора
  ([] [])
  ;; финализация аккумулятора
  ([acc] acc)
  ;; одиночный "шаг", получающий новое значение аккумулятора
  ;; из старого значения аккумулятора и значения текущего элемента
  ;; сворачиваемой последовательности
  ([acc v]
   (if (even? v)
     (conj acc v)
     acc)))

(comment
  (reduce conj-even [] (range 10))
  ;; => [0 2 4 6 8]
  )

;; ** transduce

(comment
  ;; transduce не требует указания начального значения аккумулятора,
  ;; вместо этого вызывает (conj-even) перед началом сворачивания
  ;; и (conj-even acc) в конце сворачивания
  (transduce identity conj-even (range 10))
  ;; => [0 2 4 6 8]
  )

(transduce (filter even?) conj (range 10))

(defn conj-tr
  "Работает как conj, но выводит свои аргументы при вызове."
  ([]
   (println "(conj-tr) ;; init")
   [])
  ([acc]
   (println (str "(conj-tr " acc ") ;; finalize"))
   acc)
  ([acc v]
   (println (str "(conj-tr " acc " " v ") ;; step"))
   (conj acc v)))

(comment
  (transduce identity conj-tr (range 5))
  ;; (conj-tr) ;; init
  ;; (conj-tr [] 0) ;; step
  ;; (conj-tr [0] 1) ;; step
  ;; (conj-tr [0 1] 2) ;; step
  ;; (conj-tr [0 1 2] 3) ;; step
  ;; (conj-tr [0 1 2 3] 4) ;; step
  ;; (conj-tr [0 1 2 3 4]) ;; finalize
  )

(defn avg
  "Сворачивает последовательность чисел в среднее значение."
  ([] [0 0])
  ([[s c]] (float (/ s c)))
  ([[s c] v] [(+ s v) (inc c)]))

(comment
  (transduce identity avg (range 10))
  ;; => 4.5
  )

(defn xmap [f]
  "Упрощённый аналог map, работающий только как трасдьюсер."
  (fn [reducer]
    (fn
      ([]
       (reducer))
      ([acc]
       (reducer acc))
      ([acc v]
       (reducer acc (f v))))))

(defn xfilter [f]
  "Упрощённый аналог filter, работающий только как трасдьюсер."
  (fn [reducer]
    (fn
      ([] (reducer))
      ([acc] (reducer acc))
      ([acc v]
       (if (f v)
         (reducer acc v)
         (reducer acc))
       ))))

(comment
  (= (transduce
      (comp (xfilter odd?)
            (xmap str))
      conj
      (range 10))

     (transduce
      ;; это уже встроенные map и filter, работающие в режиме
      ;; трансдьюсеров
      (comp (filter odd?)
            (map str))
      conj
      (range 10)))
  ;; => true
  )

;; ** отладка

(defn trace [prefix]
  (fn [reducer]
    (fn [& args]
      (println (str prefix ": " args))
      (apply reducer args))))

(comment
  (transduce
   (comp (filter odd?)
         (map str)
         (trace "trace"))
   conj
   (range 10))
  ;; trace: ([] "1")
  ;; trace: (["1"] "3")
  ;; trace: (["1" "3"] "5")
  ;; trace: (["1" "3" "5"] "7")
  ;; trace: (["1" "3" "5" "7"] "9")
  ;; trace: (["1" "3" "5" "7" "9"])
  ;; заметьте, что инициализация аккумулятора с помощью трансдьюсеров
  ;; не производилась — не было вызова без аргументов

  (transduce
   (comp (filter odd?)
         (map str))
   ((trace "trace") conj)
   (range 10))
  ;; trace:  ;; <- вот он, вызов без аргументов
  ;; trace: ([] "1")
  ;; trace: (["1"] "3")
  ;; trace: (["1" "3"] "5")
  ;; trace: (["1" "3" "5"] "7")
  ;; trace: (["1" "3" "5" "7"] "9")
  ;; trace: (["1" "3" "5" "7" "9"])
  ;; редьюсер был вызван без аргументов для инициализации аккумулятора
  )

;; ** into

(comment
  (into '() (map inc) (range 5))
  ;; => (5 4 3 2 1)
  (into [] (map inc) (range 10))
  ;; => [1 2 3 4 5]
  )

;; ** sequence

(comment
  (let [touch (fn [rf]
                (fn
                  ([acc] (rf acc))
                  ([acc v]
                   (println "step")
                   (rf acc v))))
        s (take 3 (sequence touch (range 1000)))]
    [(count s)
     (count s)]
    )
  ;; step
  ;; step
  ;; ...
  ;; step ;; итого 32 раза
  ;; => [3 3]
  ;; при вычислении второго count последовательность заново не вычислялась,
  ;; так что некоторая ленивость пристуствует.
  ;; Но и вычислены были 32 элемента, а не 3 и не 1000 — sequence
  ;; обрабатывает элементы порциями!
  )
