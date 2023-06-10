## Clojure Developer. Урок 10

Полиморфизм в Clojure. Часть 2

* [Datatypes: deftype, defrecord and reify](https://clojure.org/reference/datatypes)
* [Protocols](https://clojure.org/reference/protocols)
* [Clojure from the ground up: polymorphism](https://aphyr.com/posts/352-clojure-from-the-ground-up-polymorphism)

> When performance matters, we turn to interfaces and protocols

* [definterface](https://clojuredocs.org/clojure.core/definterface)
* [reify](https://clojuredocs.org/clojure.core/reify)
* [proxy](https://clojuredocs.org/clojure.core/reify)

### Протоколы

> "It is better to have 100 functions operate on one data structure than to have 10 functions operate on 10 data structures."
-- Alan Perlis' Epigrams on Programming, 1982

[Expression Problem](https://wiki.c2.com/?ExpressionProblem)

* [defprotocol](https://clojuredocs.org/clojure.core/defprotocol)
* [extend-protocol](https://clojuredocs.org/clojure.core/extend-protocol)
* [deftype](https://clojuredocs.org/clojure.core/deftype)
* [defrecord](https://clojuredocs.org/clojure.core/defrecord)

### deftype vs defrecord

* `deftype` provides no functionality not specified by the user, other than a constructor
* `defrecord` provides a complete implementation of a persistent map, including:
  * value-based equality and hashCode
  * metadata support
  * associative support
  * keyword accessors for fields
  * extensible fields (you can assoc keys not supplied with the defrecord definition)
  * etc
* `deftype` supports mutable fields, defrecord does not
* `defrecord` supports an additional reader form of `#my.record{:a 1, :b 2}`
* when a `defrecord` `Bar` is defined a corresponding function `map->Bar` is defined that takes a map and initializes a new record instance with its contents
