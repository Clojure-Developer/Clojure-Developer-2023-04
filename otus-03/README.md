## Clojure Developer. Урок 3

Функциональное программирование в Clojure, функции высшего порядка, рекурсия и композиция.

![](https://clojure.org/images/content/guides/learn/syntax/structure-and-semantics.png)

[eval](https://clojuredocs.org/clojure.core/eval)

* функция
* макрос
* [особая форма](https://clojure.org/reference/special_forms)


### Принципы ФП

<https://clojure.org/about/functional_programming>

Функции — объекты первого класса.
* могут участвовать в выражениях;
* могут быть сохранены в переменные или быть частью составной структуры данных;
* могут быть переданы в функции как аргументы;
* могут быть возвращены из функции как результаты.

Всё является выражением; функции возвращают значение последней формы.

* "ленивые" последовательности
* [иммутабельные структуры данных](https://cambridge.org/core/books/purely-functional-data-structures/0409255DA1B48FA731859AC72E34D494)
* "чистые" функции
  * ссылочная прозрачность (referential transparency)


### Способы создания функций

[functions.clj](src/otus_03/functions.clj)

* [безымянная `fn`](https://clojuredocs.org/clojure.core/fn)
* [литерал `#()`](https://clojure.org/guides/higher_order_functions#_function_literals)
* [поименованная `defn`](https://clojuredocs.org/clojure.core/defn)

#### Дополнительные возможности

* функции с переменным числом аргументов
* мультиарные функции
* [инварианты](https://clojure.org/reference/special_forms#_fn_name_param_condition_map_expr)


### Замыкания

> On his next walk with Qc Na, Anton attempted to impress his master by saying “Master, I have diligently studied the matter, and now understand that objects are truly a poor man’s closures.” Qc Na responded by hitting Anton with his stick, saying “When will you learn? Closures are a poor man’s object.” At that moment, Anton became enlightened.

<http://people.csail.mit.edu/gregs/ll1-discuss-archive-html/msg03277.html>


### Способы вызова функций

* (тривиальный)
* [apply](https://clojuredocs.org/clojure.core/apply)
* [partial](https://clojuredocs.org/clojure.core/partial)
* [complement](https://clojuredocs.org/clojure.core/complement)
* threading macro: [->](https://clojuredocs.org/clojure.core/-%3E), [->>](https://clojuredocs.org/clojure.core/-%3E%3E)


### Рекурсия

[recur](https://clojuredocs.org/clojure.core/recur)

[trampoline](https://clojuredocs.org/clojure.core/trampoline)


### Цикл loop

[loop](https://clojuredocs.org/clojure.core/loop)


### Функции высшего порядка в стандартной библиотеке

* [reduce](https://clojuredocs.org/clojure.core/reduce)
* [range](https://clojuredocs.org/clojure.core/range)
* [memoize](https://clojuredocs.org/clojure.core/memoize)
* [juxt](https://clojuredocs.org/clojure.core/juxt)
* [fnil](https://clojuredocs.org/clojure.core/fnil)
* [iterate](https://clojuredocs.org/clojure.core/iterate)
* [partial](https://clojuredocs.org/clojure.core/partial)
* [some-fn](https://clojuredocs.org/clojure.core/some-fn)
* [every-pred](https://clojuredocs.org/clojure.core/every-pred)
