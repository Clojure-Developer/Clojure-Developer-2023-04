(ns otus-24.core
  (:require [datascript.core :as d]))

;; * Basic queries

;; базу представим в виде вектора триплов
(def mages
  [[:s/fb   :spell/name     "Fireball"]
   [:s/fb   :spell/manacost 100]
   [:s/fsb  :spell/name     "Frostbolt"]
   [:s/fsb  :spell/manacost 100]
   [:s/heal :spell/name     "Heal"]
   [:s/heal :spell/manacost 200]

   [:m/david :mage/name   "David"]
   [:m/david :mage/age    25]

   [:m/john  :mage/name   "John"]
   [:m/john  :mage/age    40]
   [:m/john  :mage/spells :s/fb]
   [:m/john  :mage/spells :s/fsb]

   [:m/ivan  :mage/name   "Ivan"]
   [:m/ivan  :mage/age    200]
   [:m/ivan  :mage/spells :s/heal]])

;; простейший запрос к данным
(d/q '[:find ?name                    ; `?name` - pattern variable
       :where [_ :mage/name ?name]]   ; `_` - игнорирование значения
     ;; в качестве источников данных можно использовать нативные структуры
     mages)

;; вытаскиваем пары id -> name
(d/q '[:find ?e ?name
       :where [?e :mage/name ?name]]
     mages)

;; запрос id по имени
(d/q '[:find ?e .                     ; `.` в конце означает, что мы ожидаем одно значение
       :where [?e :mage/name "John"]]
     mages)

(d/q '[:find ?e .
       :in $ ?name                    ; передаем параметры в запрос снаружи
       :where [?e :mage/name ?name]]
     mages "John")

;; `?e` принимает одно и тоже значение во всех условиях внутри запроса
(d/q '[:find ?age .
       :where
       [?e :mage/name "John"]
       [?e :mage/age ?age]]
     mages)

;; Задача. Получить список заклинаний выборанного мага по имени

(d/q '[:find ?spell
       :where]
     mages)

;; Предикаты внутри запроса

(d/q '[:find ?name
       :where
       [?e :mage/name ?name]
       [?e :mage/age ?age]
       [(> ?age 100)]]                  ; запрашиваем всех, кто старше 100 лет
     mages)

(d/q '[:find ?name ?centuries
       :where
       [?e :mage/name ?name]
       [?e :mage/age ?age]
       [(/ ?age 100) ?hths]             ; делим возраст на сто и связываем результат с промежуточной переменной `?hths`
       [(clojure.core/int ?hths) ?centuries]] ; округляем вниз и связываем с `?centuries`
     mages)

;; * Transactions

;; Создадим `connection` к пустой базе без схемы
(def conn (d/create-conn))

;; Можно передать данные для записи в базу ввиде мап
(d/transact! conn [{:mage/name "David"
                    :mage/age 25}
                   {:mage/name "John"
                    :mage/age 40}
                   {:mage/name "Ivan"
                    :mage/age 200}])

;; Определим схему базы данных и создадим новую базу с данной схемой
;; Для DataScript схема не обязательна в отличие от Datomic
(def schema
  {:mage/name        {:db/cardinality :db.cardinality/one
                      :db/unique :db.unique/identity
                      :db/doc "A mage's name"}
   :mage/age         {:db/cardinality :db.cardinality/one}
   :mage/spells      {:db/valueType :db.type/ref
                      :db/cardinality :db.cardinality/many
                      :db/doc "Spellbook, cons"}
   :spell/name       {:db/cardinality :db.cardinality/one
                      :db/unique :db.unique/identity}
   :spell/manacost   {:db/cardinality :db.cardinality/one}})

(def conn (d/create-conn schema))

;; Запишем новые данные
(d/transact! conn [{:mage/name "David"
                    :mage/age 25}
                   {:mage/name "John"
                    :mage/age 75
                    :db/id "john"}
                   {:mage/name "Ivan"
                    :mage/age 200}

                   {:spell/name "Fireball"
                    :spell/manacost 100
                    :db/id "fb"}

                   {:spell/name "Frostbolt"
                    :spell/manacost 100
                    :db/id "fsb"}

                   ;; другой вариант сохранения информации о фактах в БД
                   [:db/add "john" :mage/spells "fb"]
                   [:db/add "john" :mage/spells "fsb"]])

(d/q '[:find [?spell ...]
       :in $ ?mage
       :where
       [?e :mage/name ?mage]
       [?e :mage/spells ?s]
       [?s :spell/name ?spell]
       ;; дополнить запрос
       ]
     (d/db conn) "John")

(d/transact! conn [[:db/retract [:mage/name "John"] :mage/spells [:spell/name "Fireball"]]]) ;; факт о том, что какие-то данные были удалены

;; * Advanced queries. Pulls and Aggregates
;; like SELECT *
(d/pull (d/db conn)                     ; db
        '[*]                            ; pull pattern
        [:mage/name "John"])            ; entity

;; аналогичный запрос
(d/q '[:find (pull ?e [*])
       :where
       [?e :mage/name "John"]]
     (d/db conn))

;; Ограничить выборку конкретных полей
(d/pull (d/db conn) '[:db/id :mage/age :mage/spells] [:mage/name "John"])

;; Вложенные атрибуты
(d/pull (d/db conn) '[:db/id :mage/age {:mage/spells [:spell/name]}] [:mage/name "John"])

;; Комбинируем с `*`
(d/pull (d/db conn) '[* {:mage/spells [*]}] [:mage/name "John"])

;; Reverse lookups
(d/pull (d/db conn) '[{:mage/_spells [:mage/name]}] [:spell/name "Fireball"])

;; Рекурсинвые запросы
(def schema-with-recursion
  (merge schema {:mage/apprentices {:db/valueType :db.type/ref
                                    :db/cardinality :db.cardinality/many}}))

(def conn (d/create-conn schema-with-recursion))

(d/transact! conn [{:mage/name "David"
                    :mage/age 25
                    :db/id "davi"}
                   {:mage/name "John"
                    :mage/age 75
                    :db/id "john"}
                   {:mage/name "Ivan"
                    :mage/age 200
                    :db/id "ivan"}

                   {:spell/name "Fireball"
                    :spell/manacost 100
                    :db/id "fb"}

                   {:spell/name "Frostbolt"
                    :spell/manacost 100
                    :db/id "fsb"}

                   {:spell/name "Heal"
                    :spell/manacost 200
                    :db/id "heal"}

                   [:db/add "john" :mage/spells "fb"]
                   [:db/add "john" :mage/spells "fsb"]
                   [:db/add "ivan" :mage/spells "heal"]

                   [:db/add "ivan" :mage/apprentices "john"]
                   [:db/add "john" :mage/apprentices "davi"]])

(d/pull (d/db conn) '[:mage/name :mage/age {:mage/apprentices ...}] [:mage/name "Ivan"])
 ;; =>
 ;; {:mage/age 200
 ;;  :mage/apprentices [{:mage/age 40
 ;;                      :mage/apprentices [{:mage/age 25
 ;;                                          :mage/name "David"}]
 ;;                      :mage/name "John"}]
 ;;  :mage/name "Ivan"}

;; aggregate fns
;; single values (e.g. `max`, `min`, `sum`, `avg`, `count`)
;; colls (e.g. `(distinct ?xs)` `(min n ?xs)` `(max n ?xs)` `(rand n ?xs)` `(sample n ?xs)`)
;; средний возраст
(-> (d/q '[:find (avg ?age) .
           :where [_ :mage/age ?age]]
         (d/db conn))
    int)

;; количество заклинаний у каждого мага
(d/q '[:find ?mage (count ?spell)
       :where
       [?e :mage/name ?mage]
       [?e :mage/spells ?spell]]
     (d/db conn))

;; * Advanced queries. Rules

(d/q '[:find ?mage
       :in $ %
       :where
       (skilled-mage ?e)
       [?e :mage/name ?mage]]
     (d/db conn)
     '[[(skilled-mage ?e)
        [?e :mage/spells]]])

(d/q '[:find ?mage .
       :in $ % ?spell
       :where
       (mage-knows-spell ?mage ?spell)]
     (d/db conn)
     '[[(mage-knows-spell ?mage ?spell)
        [?e :mage/name ?mage]
        [?s :spell/name ?spell]
        [?e :mage/spells ?s]]]
     "Heal")
