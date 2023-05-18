## Clojure Developer. Урок 4

Персистентные структуры данных в Clojure, работа с коллекциями.

### Персистентные структуры данных

Всё есть дерево. Даже списки, которые являются вырожденными деревьями.

Подробнее можно почитать тут: [Understanding Clojure's Persistent Vectors, pt. 1](https://hypirion.com/musings/understanding-persistent-vector-pt-1)

Суть: ПСД устроены так, что любое изменение порождает новую версию структуры, которая отличается от оригинала только теми частями, которые были затронуты изменениями.

### Структуры из стандартной поставки Clojure

#### Списки

Односвязные списки внутри. Об этом стоит помнить. Собираются из пар значение + ссылка на "хвост".

```clojure
(cons 1 (cons 2 (cons 3 ())))
;; сахарок
(list 1 2 3)
```

Селекторы:

-   [first](https://clojuredocs.org/clojure.core/first)
-   [rest](https://clojuredocs.org/clojure.core/rest)
-   [last](https://clojuredocs.org/clojure.core/last)
-   [nth](https://clojuredocs.org/clojure.core/nth)
-   …


#### Вектора

Деревья внутри, но об этом помнить не нужно. Предоставляют быстрый доступ по индексу.

```clojure
(vector 1 2 3)
(vec '(1 2 3))
;; сахарок
[1 2 3]
```

Селекторы:

-   [nth](https://clojuredocs.org/clojure.core/nth)

first/last и подобные использовать можно, но стоит помнить о цене.


#### Отображения (maps)

Хранят соответствие уникальных ключей и неуникальных значений. Предоставляют быстрый доступ по ключу. Порядок добавления ключей **не запоминается**.

```clojure
(hash-map :a 1 :b "foo")
;; сахарок
{:a 1 :b "foo"}
```

Селекторы:

-   [get](https://clojuredocs.org/clojure.core/get)
-   [contains?](https://clojuredocs.org/clojure.core/contains_q)

first/last и подобные использовать можно, но стоит помнить о цене.


#### Множества

Помнят о том, что в них было добавлено, но не помнят, в каком порядке. Хранят уникальные значения, быстро проверяют вхождение элемента.

```clojure
(set '(1 2 3))
;; сахарок
#{1 2 3}
```

Селекторы:

-   [contains?](https://clojuredocs.org/clojure.core/contains_q)

Соотношение множеств делается с помощью функций в модуле [clojure.set](https://clojuredocs.org/clojure.set)

## Работа с коллекциями

### Обобщённый доступ к содержимому

[get](https://clojuredocs.org/clojure.core/get)

```clojure
(get [1 2 3] 0)        ;; => 1
(get [1 2 3] 5)        ;; => nil
(get [1 2 3] 5 :oops)  ;; => oops
(get {:a 42} :a)       ;; => 42
(get {:a 42} :b)       ;; => nil
(get {:a 42} :b :oops) ;; => :oops
(get #{1 2 3} 1)       ;; => 1
(get #{1 2 3} 5)       ;; => nil
(get #{1 2 3} 5 :oops) ;; => :oops
```


### Модификация


### Добавление и замена элементов

[conj](https://clojuredocs.org/clojure.core/conj), [assoc](https://clojuredocs.org/clojure.core/assoc)

```clojure
(conj '(1 2 3) 4)      ;; => '(4 1 2 3)
(conj [1 2 3] 4)       ;; => [1 2 3 4]
(conj #{1 2 3} 3)      ;; => #{1 3 2}
(conj #{1 2 3} 4)      ;; => #{1 4 3 2}
(conj {:a 1} [:a 2])   ;; => {:a 2}
(conj {:a 1} [:b 2])   ;; => {:a 1, :b 2}

(assoc [1 2 3] 0 :foo) ;; => :foo
(assoc [1 2 3] 5 :foo) ;; => Execution error
(assoc {:a 1} :a 2)    ;; => {:a 2}
(assoc {:a 1} :b 2)    ;; => {:a 1, :b 2}
```


### Обновление

[update](https://clojuredocs.org/clojure.core/update)

```clojure
(update [1 2 3] 0 inc)  ;; => [2 2 3]
(update [1 2 3] 0 + 10) ;; => [11 2 3]
(update {:a 1} :a inc)  ;; => {:a 2}
```


### Удаление ключей

[dissoc](https://clojuredocs.org/clojure.core/dissoc)

```clojure
(dissoc {:a 1 :b 2} :a) ;; => {:b 2}
```


### Работа с вложенными структурами

[get-in](https://clojuredocs.org/clojure.core/get-in), [assoc-in](https://clojuredocs.org/clojure.core/assoc-in), [update-in](https://clojuredocs.org/clojure.core/update-in)

```clojure
(def data {:cart [{:type :apple :amount 1}]})
(get-in data [:cart 0 :amount])
;; => 1
(update-in data [:cart 0 :amount] inc)
;; => {:cart [{:type :apple, :amount 2}]}
```


### Деструктуризация

Подробнее можно почитать тут: [Destructuring in Clojure](https://clojure.org/guides/destructuring)

Суть: в let и в описании параметров функций можно указать желаемую структуру с именованием отдельных её частей. В последствии данные будут сопоставлены с желаемой структурой, и если совпадение будет полным, вы получите именованные части в виде локальных определений.

```clojure
(def data {:cart [{:type :apple :amount 1}]})

(let [{[{t :type a :amount} & _] :cart} data]
  [t a])
;; => [:apple 1]
```


### Обработка коллекций как последовательностей


#### Отображение (mapping)

[map](https://clojuredocs.org/clojure.core/map), [mapv](https://clojuredocs.org/clojure.core/mapv)

```clojure
(map inc '(1 2 3))     ;; => (2 3 4)
(map inc [1 2 3])      ;; => (2 3 4)
(mapv inc [1 2 3])     ;; => [2 3 4]
(map last {:a 1 :b 2}) ;; => (1 2)

(map + [1 2 3] [10 20 30 40]) ;; (11 22 33)
```


#### Фильтрация

[filter](https://clojuredocs.org/clojure.core/filter)

```clojure
(filter odd? [1 2 3 4 5]) ;; => (1 3 5)
```


#### Агрегация

[reduce](https://clojuredocs.org/clojure.core/reduce)

```clojure
(reduce + [1 2 3 4 5])      ;; => 15
(reduce + 1000 [1 2 3 4 5]) ;; => 1015
(reduce conj [1 2 3] [4 5 6])
;; => [1 2 3 4 5 6]
```


#### List comprehensions

[for](https://clojuredocs.org/clojure.core/for)

```clojure
(for [x [10 20 30]
      y [1 2 3]
      :let [sum (+ x y)]
      :while (< sum 23)]
  sum)
;; => (11 12 13 21 22)
(let [deltas [-1 0 1]]
  (for [dx deltas
        dy deltas
        :let [pair [dx dy]]
        :when (not= pair [0 0])]
    pair))
;; => ([-1 -1] [-1 0] [-1 1] [0 -1] [0 1] [1 -1] [1 0] [1 1])
```

### Коллекции как функции

```clojure
({:a 1 :b 2} :c)  ;; => nil
({:a 1 :b 2} :a)  ;; => 1
(["a" "b" "c"] 1) ;; => "b"
(#{1 2 3} 1)      ;; => 1
(#{1 2 3} 4)      ;; => nil
```

Ключевые слова работают как селекторы для отображений:

```clojure
(:a {:a 1 :b 2}) ;; => 1
(:c {:a 1 :b 2}) ;; => nil
```
