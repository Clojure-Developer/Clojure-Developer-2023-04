# otus-19:  Реактивность

## [Communicating Sequential Processes](https://en.wikipedia.org/wiki/Communicating_sequential_processes)

Сопроцедуры плюс каналы.


## [Reactive Programming](https://en.wikipedia.org/wiki/Reactive_programming)


### Pull

Итераторы, трансдьюсеры — системы, где потребитель данных форсирует протаскивание очередной порции.

```clojure
(for [i (map inc (filter odd? (range 1 10)))]
  (print "..."))
```


### Push

React+Redux, The Elm Architecture — системы, где источник данных проталкивает порции данных через конвейер обработчиков до потенциального потребителя.

> model -> view -> event -> model' -> view' -> event' -> model'' -> ...


### [Reactive extensions](https://reactivex.io)

-   [Observables](https://reactivex.io/documentation/observable.html) производят данные.
-   Observers обрабатывают переданные данные и могут передать результаты дальше — в этом случае observers становятся observable.
-   [Операторы](https://reactivex.io/documentation/operators.html) позволяют преобразовывать данные, проталкиваемые через конвейер. Конвейер может разветвляться и собираться с распараллеливанием и без оного — всё очень гибко.

С Clojure можно использовать в виде [rx-clojure](https://github.com/Vikasg7/rx-clojure/).


## [Functional Reactive Programming](https://reactivex.io/documentation/operators.html)

Всё похоже на формулы Excel, максимально декларативно описанные соотноешния между "значениями во времени".

```clojure
(scene ;; тут осуществляет побочный эффект - сцена отрисовывается
 (circle
  (map second
       (zip ;; тут события собиратся в пары
        (filter left-button-down? mouse-button) ;; факты нажатия на кнопку мыши
        (map
         ;; наложение на сетку
         (partial map (fn [coord] (* 10 (mod coord 10))))
         (filter in-window? ;; ограничение перемещения размерами окна
                 mouse-positions ;; позиция мыши "во времени"
                 ))))))
```


## [Reactive Manifesto](https://www.reactivemanifesto.org)

Создаём отзывчивые, надежные, эластичные системы, где всё управляется посылкой сообщений.


### [Модель акторов](https://en.wikipedia.org/wiki/Actor_model)

[Akka](https://akka.io) для Java & Scala, можно использовать с Clojure ([akka-clojure](https://github.com/setrar/akka-clojure)).

```clojure
(def counter
  (spawn (fn [{:keys [value sender]} state]
           (match value
                  :inc (inc state)
                  :dec (dec state)
                  :get (! sender state)))))

(! counter :inc)
```
