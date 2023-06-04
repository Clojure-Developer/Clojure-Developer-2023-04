## Clojure Developer. Урок 7

Регулярные выражения, очереди и list comprehension.

### Регулярные выражения

![](https://imgs.xkcd.com/comics/perl_problems.png)

* [regex101](https://regex101.com)
* [Clojure Regex Tutorial](https://ericnormand.me/mini-guide/clojure-regex)
* [Regular Expressions - Practicalli Clojure. Option flags](https://practical.li/clojure/reference/standard-library/regular-expressions/#option-flags)
* [Regex Tutorial - Unicode Characters and Properties](https://regular-expressions.info/unicode.html)

Функции:
* [re-pattern](https://clojuredocs.org/clojure.core/re-pattern)
* [re-matches](https://clojuredocs.org/clojure.core/re-matches)
* [re-find](https://clojuredocs.org/clojure.core/re-find)
* [re-seq](https://clojuredocs.org/clojure.core/re-seq)
* (не рекомендуемые) [re-matcher](https://clojuredocs.org/clojure.core/re-matcher), [re-groups](https://clojuredocs.org/clojure.core/re-groups)
* [clojure.string/replace](https://clojuredocs.org/clojure.string/replace)
* [clojure.string/replace-first](https://clojuredocs.org/clojure.string/replace-first)
* [clojure.string/split](https://clojuredocs.org/clojure.string/split)

Библиотеки:
* [regal](https://github.com/lambdaisland/regal), [интерактивное руководство](https://lambdaisland.github.io/land-of-regal)

### Иммутабельные очереди (FIFO)

[PersistentQueue.java](https://github.com/clojure/clojure/blob/master/src/jvm/clojure/lang/PersistentQueue.java)

> Clojure's persistent queue is implemented internally using two separate collections, the front being a seq and the rear being a vector, as shown in the figure below. All insertions occur in the rear vector, and all removals occur in the front seq, taking advantage of each collection's strength. When all the items from the front list have been popped, the back vector is wrapped in a seq to become the new front, and an empty vector is used as the new back.

![](https://drek4537l1klr.cloudfront.net/fogus2/Figures/05fig05.jpg)

[Queues in Clojure - Michael Zavarella](https://admay.github.io/queues-in-clojure)

### List comprehension

* [for](https://clojuredocs.org/clojure.core/for)

### Функции семейства do

* [doseq](https://clojuredocs.org/clojure.core/doseq)
* [dorun](https://clojuredocs.org/clojure.core/dorun)
* [doall](https://clojuredocs.org/clojure.core/doall)
* [dotimes](https://clojuredocs.org/clojure.core/dotimes)
* [run!](https://clojuredocs.org/clojure.core/run!)
