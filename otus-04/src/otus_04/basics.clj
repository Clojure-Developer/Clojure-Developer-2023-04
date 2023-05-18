(ns otus-04.basics)

;; * Описание коллекций
;; ** Списки

'(list 1 (+ 2 5) 3 4)
(list 1 (+ 2 5) 3 4)
'(1 (2 + 5) / 3 * 4)

(cons 1 (cons 2 (cons 3 ())))

;; ** Вектора

(vector 1 2 3)
[1, 2, 3]

(vec '(1 2 3)) ;; converts smth to vector

;; ** Отображения

(hash-map :a 1
          :b 3
          :c 4)

{:a 1
 "asd" 3
 [12 45 "red"] 42}

;; ** Множества

#{1 2 3}
#{[1] () "asd" 43}

(set '(1 2 2 3))

;; * Получение доступа к элементам
;; ** get,nth,first,last,rest

(get [:a :b :c] 5 :oops)
(get {:a 1 :b 2} :c)
(get nil :c)
(get '(1 2 3) :c) ;; always nil
(get #{1 2 3} 5)

(first {:a 1}) ;; returns a pair

;; ** Коллекции как функции

({:a 1 :b 2} :a)

(#{1 2 3} 4)

(:a {:a 1 :b 2})

;; * Модификация коллекций
;; ** Добавление

(conj {} [:a 1] [:b 2])

(conj '(1 2 3) 4)
(pop '(4 1 2 3))

(defn test-as-stack [coll]
  (let [s (conj coll 4)]
    [(peek s)
     (pop s)]))

(test-as-stack [1 2 3])

(conj #{1 2 3} 3)

(assoc [1 2 3] 1 :foo)
(assoc {:a 1 :b 2} :a 100)

;; ** Удаление ключей

(dissoc {:a 1 :b 2} :a)

;; ** Обновление

(update [1 2 3] 1 + 100 50)
(update {:a 1 :b 2} :a + 100 50)

;; * Работа с последовательностями
;; ** Отображение значений

(map + '(1 2 3) [10 20 30 40])
(map identity {:a 1 :b 22})
(mapv + '(1 2 3) [10 20 30 40])

;; ** Фильтрация

(filter odd? [1 2 3 4 5])
(filterv odd? [1 2 3 4 5])

;; ** Агрегация

(reduce + 10 [1 2 3]) ;; (10 + 1 + 2 + 3)
(reduce conj [] '(1 2 3 4))
(reduce conj [] ())

;; ** List comprehensions

(for [x [1 2 3]
      y [10 20 30]
      :let [z (+ x y)]
      :while (< z 25)]
  z)

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

(get (last (get data :users)) :name)
(get-in data [:users 0 :pets 0 :name])
(update-in data [:users 0 :age] inc)

;; * Деструктуризация

(let [coords [100 [200 3 4 4 5 ]]
      [_ & ys] coords]
  ys)

(let [point {:x 100 :y 200 :color "red"}
      {:keys [x y]} point]
  [x y])

(defn first-users-name
  [{[{n :name} & _] :users}]
  n)

(first-users-name data)

(for [{ps :pets} (:users data)
      {n :name} ps]
  n)

(set
 (for [{ps1 :pets n1 :name} (:users data)
       {ps2 :pets n2 :name} (:users data)
       :when (not= n1 n2)
       {k1 :kind} ps1 :when (= k1 :cat)
       {k2 :kind} ps2 :when (= k2 :cat)]
   (set [n1 n2])))
