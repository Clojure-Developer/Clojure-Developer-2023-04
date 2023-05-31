(ns otus-8.core
  (:gen-class))

(defn now
  []
  (java.util.Date.))

(set! *print-meta* true)

;; Работа с метаданными через функции

;; К чему можно прикреплять метаданные?

(with-meta [1 2 3]
  {:created-on (now)})

(with-meta '(1 2 3)
  {:created-on (now)})

(with-meta 'sym
  {:created-on (now)})

(with-meta #{1 2 3}
  {:created-on (now)})

(with-meta {:a 1 :b 2}
  {:created-on (now)})

;; К чему нельзя прикреплять метаданные?

(with-meta :keyword
  {:created-on (now)})

(with-meta "string"
  {:created-on (now)})

(with-meta (java.util.Base64/getDecoder)
  {:created-on (now)})

;; Метаданные персистентны

(set! *print-meta* false)

(def a
  (with-meta [1 2 3]
    {:created-on (now)}))

(meta a)

(def b
  (vary-meta a assoc :updated-on (now)))

(meta b)

;; Метаданные не влияют на сравнений

(= a b)

;; Работа с метаданными через reader-macro

(meta ^{:a true :b true} [1 2 3])

(meta ^:private ^:dynamic [1 2 3])

(meta ^String [1 2 3])

(meta ^String ^Long [1 2 3])

;; Warning!!!!!!!!!!!

; Очередь вычисления кода в Clojure;
; 1) Reader macro;
; 2) Макросы;
; 3) Функции.

(meta #^:foo (quote (1 2 3)))

; 1)
; => (quote (1 2 3)) with meta

; 2-3)
; => (1 2 3) as list without meta
