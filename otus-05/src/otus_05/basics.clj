(ns otus-05.basics)

;; * Ловим исключения
(try
  (println (assoc [] 5 :foo))
  (catch java.lang.IndexOutOfBoundsException e
    (println "catched!")
    [:oops])
  (finally
    (println "finally!")))

;; * Работаем со StringBuilder
;; ** импортируем
;; ** инстанциируем
;; ** методы и поля
;; ** doto
;; * Пример локального Java-класса

(import [otus_05 Point])

;; * Пример gen-class

(import [otus_05.rgb Rgb])
