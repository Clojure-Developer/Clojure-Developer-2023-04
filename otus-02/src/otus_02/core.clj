(ns otus-02.core
  (:require [otus-02.conditionals]
            [otus-02.conditionals :as conditionals]
            [otus-02.conditionals :refer [ten eleven]])
  (:use [otus-02.conditionals])
  (:refer-clojure :exclude [test])
  (:import [java.io.File]))


;; otus-02.core    имя нейспейса
;; otus_02/core    путь на файловой системе



;; текущий неймспейс
*ns*

(import '[java.io.File])

;; подключаемся в другой неймспейс
(in-ns 'otus-02.conditionals)

;; не существующий нейспейс
(in-ns 'ns-wo-file)

;; дефолтный неймспейс
(in-ns 'user)

(in-ns 'otus-02.core)



;; подключаем другой неймспейс
(require '[otus-02.conditionals])

;; алиас
(require '[otus-02.conditionals :as cnd :reload-all])

;; импортируем только то что нужно
(require '[otus-02.conditionals :refer [ten eleven]])
(require '[otus-02.conditionals])
(require '[otus-02.conditionals :refer [eleven] :rename {eleven twelve}])
(println eleven)

(use '[otus-02.conditionals])


(ns-publics 'otus-02.conditionals)

(ns-resolve 'otus-02.conditionals 'eleven)

(find-ns 'my-new-namespace)
(create-ns 'my-new-namespace)

(require '[clojure.java.classpath])


(System/getProperty "java.class.path")

(require '[clojure.repl :refer :all])

(doc require)