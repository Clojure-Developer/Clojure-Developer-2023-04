(ns otus-15.future)

;;
;; Механизм future.
;;

(def long-calculation
  (future
    (Thread/sleep 5000)
    :done!))

;; Получение значения с таймаутом.
(deref long-calculation 1000 :impatient!)

;; Получение значения.
(deref long-calculation)

;; Получение значения путём применения синтаксического сахара.
@long-calculation

;; Возвращает true, если значение было вычислено для future,
;; promise, delay или ленивой последовательности.
(realized? long-calculation)

;; Футуры запоминают свои значения, таким образом после их
;; однократного вычисления, они не будут перезапускаться.