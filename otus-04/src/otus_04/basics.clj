(ns otus-04.basics)

;; * Описание коллекций
;; ** Списки

'(1 2 3)
(quote (1 2 3))
'(1 (+ 1 1) 3)
'(1 2 3)

(list 1 (+ 1 1) 3)

(cons 1 (cons 2 (cons 3 ())))

;; ** Вектора

(vector 1 2 3)
[1 (+ 1 1) 3]
(vec '(1 2 3))

;; ** Отображения

{:a 1
 :b 2
 "asd" 3
 [:a '(7 'foo)] :oops}

(hash-map :a 1
          :b 3)

;; ** Множества

#{1 2 3}

(set [1 2 2 3])

;; * Получение доступа к элементам
;; ** get,nth,first,last,rest

(first '(1 2 3))
(first [1 2 3])
(first {:a 1 :b 2 :c 3})
(last '(1 2 3))
(last [1 2 3])

(nth [:a :b :c] 5 :oops)
(nth '(:a :b :c) 2)

(next (next [1]))

(get [:a :b :c] 5 :oops)
(get {:a 1 :b 2} :c :oops)

(get (get {} :a) 1)
(get #{1 2 3} 2)

;; ** Коллекции как функции

({:a 1 :b 2} :a)

(#{1 2 3} 2)

(:a {:a 1 :b 2})

;; * Модификация коллекций
;; ** Добавление

(conj '(1 2 3) 4 5)
(conj [1 2 3] 4 5)
(conj {:a 1 :b 2} [:c 3] [:a 5])
(conj #{1 2} 3)

(assoc {:a 1 :b 2} :c 3 :d 5)

;; ** Удаление ключей

(dissoc {:a 1 :b 2} :a)

;; ** Обновление

(assoc {:a 1} :a 3)
(assoc [1 2 3] 3 :foo)

(update [1 2 3] 1 + 100 50) ;; (+ 2 100 50)
(update {:a 1 :b 2} :a inc)

;; * Работа с последовательностями
;; ** Отображение значений

(map inc '(1 2 3))
(map dec [4 5 6])
(mapv dec [4 5 6])

(map identity {:a 1 :b 2})

(map + [1 2 3] '(10 20 30 40))

;; ** Фильтрация

(filter odd? '(1 2 3 4 5))
(filterv odd? '(1 2 3 4 5))

;; ** Агрегация

(reduce + [1 2 3 4 5]) ;; (1 + 2 + 3 + 4 + 5)
(reduce + 100 [1 2 3]) ;; (100 + 1 + 2 + 3)
(reduce conj [] '(1 2 3 4)) ;; (conj (conj [] 1) 2) ...

;; ** List comprehensions

(for [x [1 2 3]
      y [10 20 31 40]
      :let [z (+ x y)]
      :when (odd? z)]
  )

;; * Вложенные структуры

(def data
  {:users
   [{:name "Bob"
     :age 24
     :pets [{:kind :cat
             :name "Thomas"}
            {:kind :mouse
             :name "Jerry"}]}
    {:name "Alice"
     :age 12
     :pets [{:kind :cat
             :name "Cheshire"}]}
    {:name "Shagie"
     :age 18
     :pets [{:kind :dog
             :name "Scooby Doo"}]}
    {:name "Nimnul"
     :age 50
     :pets [{:kind :cat
             :name "Fatcat"}]}]})

(get-in data [:users 0 :pets 5 :name] :oops)
(update-in data [:users 0 :age] inc)
;; data["users"][0]["age"] += 1

;; * Деструктуризация

(let [line [[10 10] [10 100]]
      [[x1 _] [x2 _]] line]
  (= x1 x2))

(let [[_ x & xs] {:a 1 :b 2 :c 3}]
  [x xs])

(let [line {:begin {:x 100 :y 100}
            :end {:x 200 :y 100}}

      {{:keys [x y]} :begin} line]
  [x y])

(defn name-of-first-user [data]
  (let [{[{n :name}] :users} data]
    n))

(name-of-first-user data)

(defn name-of-first-user [{[{n :name}] :users}]
  n)

(defn my+ [x & xs]
  (apply + x xs))

(my+ 1 2 3 4 5)

(defn vertical? [[[x1 _] [x2 _]]]
  (= x1 x2))

(let [line [[10 10] [10 100]]]
  (vertical? line))

;; ** комплексный пример
(set
 (for [{ps1 :pets n1 :name} (:users data)
       {ps2 :pets n2 :name} (:users data)
       :when (not= n1 n2)
       {k1 :kind} ps1 :when (= k1 :cat)
       {k2 :kind} ps2 :when (= k2 :cat)]
   (set [n1 n2])))
