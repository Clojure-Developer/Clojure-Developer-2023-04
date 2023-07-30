## Clojure Developer. Урок 9

Полиморфизм в Clojure. Часть 1

> I made up the term 'object-oriented', and I can tell you I didn't have C++ in mind.
> OOP to me means only messaging, local retention and protection and hiding of state-process, and extreme late-binding of all things. It can be done in Smalltalk and in LISP.

-- Alan Kay

Полиморфизм:

* [параметрический](https://ru.wikipedia.org/wiki/Параметрический_полиморфизм)
* [ad hoc](https://ru.wikipedia.org/wiki/Полиморфизм_(информатика)#Специальный_полиморфизм)
* [полиморфизм подтипов](https://ru.wikipedia.org/wiki/Полиморфизм_(информатика)#Полиморфизм_подтипов)

### case/cond

* [cond](https://clojuredocs.org/clojure.core/cond)
* [case](https://clojuredocs.org/clojure.core/case)
* [instance?](https://clojuredocs.org/clojure.core/instance_q)

### clojure.core.match

[clojure.core.match](https://github.com/clojure/core.match)

### multimethods

[Runtime Polymorphism](https://clojure.org/about/runtime_polymorphism)

[Multimethods and Hierarchies](https://clojure.org/reference/multimethods)

Multimetod:

* a *dispatching function* ([defmulti](https://clojuredocs.org/clojure.core/defmulti))
* one or more *methods* ([defmethod](https://clojuredocs.org/clojure.core/defmethod))

[methods](https://clojuredocs.org/clojure.core/remove-method)
[remove-methods](https://clojuredocs.org/clojure.core/remove-method)

[Understanding Clojure Multimethods](https://dev.to/kelvinmai/understanding-clojure-multimethods-2cd0)

[Functional Polymorphism using Clojure’s Multimethods](https://ilanuzan.medium.com/functional-polymorphism-using-clojures-multimethods-825c6f3666e6)

[Integrant](https://github.com/weavejester/integrant#usage)

#### ad-hoc иерархии

* [isa?](https://clojuredocs.org/clojure.core/isa_q)
* [derive](https://clojuredocs.org/clojure.core/derive)
* [prefer-method](https://clojuredocs.org/clojure.core/prefer-method)
* [make-hierarchy](https://clojuredocs.org/clojure.core/make-hierarchy)
* [parents](https://clojuredocs.org/clojure.core/parents)
* [ancestors](https://clojuredocs.org/clojure.core/ancestors)
* [descendants](https://clojuredocs.org/clojure.core/descendants)
