(ns otus-05.basics)

;; * Ловим исключения
(try
  ;; (throw (Exception. "oops"))

  (assoc [] 0 :foo)

  (catch java.lang.IndexOutOfBoundsException e
    (println "catched!")
    [:oops])

  (catch Exception _
    [:catched])

  (finally
    (println "finally!")))

;; * Работаем со StringBuilder
;; ** импортируем

(import [java.lang StringBuilder])

;; ** инстанциируем

(StringBuilder.)
(new StringBuilder)

;; ** методы и поля

;; new StringBuilder()
;;   .append()
;;   .append()
;;   .toString()

(-> (new StringBuilder)
    (.append "Hello ")
    (.append "World!")
    .toString)

(.. (new StringBuilder)
    (append "Hello ")
    (append "World!")
    toString)

;;  b = new StringBuilder()
;;  b.append()
;;  b.append()
;;  b.toString()

;; ** doto

(.toString
 (doto (new StringBuilder)
   (.append "hello ")
   (.append "world")))

;; * Пример локального Java-класса

(import [otus_05 Point])

(let [p (-> (new Point)
            (.offX 10.0)
            (.offY 20.5))
      [x y] p]
  [x y (.-x p)])

;; * Пример gen-class

(import [otus_05.rgb Rgb])

(.-state (new Rgb 255 0 255))

;; inport otus_05.rgb.Rgb;
