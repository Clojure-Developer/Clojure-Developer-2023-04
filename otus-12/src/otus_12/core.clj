(ns otus-12.core
  (:require [clojure.spec.alpha :as s]
            [otus-12.tagged-literals])
  (:gen-class))

;;
;; # Валидация
;;

;; ## Предикаты

(s/valid? nil? nil)
(s/valid? string? "abc")

(s/valid? #(> % 5) 10)
(s/valid? #(> % 5) 0)

(s/valid? inst? (java.util.Date.))

(s/valid? #{:club :diamond :heart :spade} :club)
(s/valid? #{:club :diamond :heart :spade} 42)

(s/valid? #{42} 42)

;; ## Registry

(s/def :order/date inst?)
(s/valid? inst? #inst "2022")
(s/valid? :order/date #inst "2022")

(s/def :order/date-alias :order/date)

(s/def :deck/suit #{:club :diamond :heart :spade})
(s/valid? :deck/suit :club)

;; Просмотр всех зарегистрированных спек.
(count (keys (s/registry)))

;; Получение формы по имени спеки, если она была зарегистрирована.
(some-> :order/date s/get-spec s/form)

(s/def :num/big-even (s/and int? even? #(> % 1000)))
(s/valid? :num/big-even :foo)
(s/valid? :num/big-even 10)
(s/valid? :num/big-even 100000)

(s/def :domain/name-or-id (s/or :name string?
                                :id int?))
(s/valid? :domain/name-or-id "abc")
(s/valid? :domain/name-or-id 100)
(s/valid? :domain/name-or-id :foo)

;; Обратите внимание, что на спеках с альтернативными вариантами (alt, or)
;; указываются имена ветвей!
(s/conform :domain/name-or-id "abc")
(s/conform :domain/name-or-id 100)

;; ## Nilable

(s/valid? string? nil)
(s/valid? (s/nilable string?) nil)

;; ## Explain

(s/explain :deck/suit 42)
(s/explain :num/big-even 5)
(s/explain :domain/name-or-id :foo)
(s/explain-str :domain/name-or-id :foo)
(s/explain-data :domain/name-or-id :foo)

;; ## Entity Maps

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(s/def ::email (s/and string? (partial re-matches email-regex)))

(s/def :account/first-name string?)
(s/def :account/last-name string?)
(s/def :account/email ::email)
(s/def :account/phone string?)

;; Мапы с квалифицированными ключами.
(s/def :account/person
  (s/keys :req [:account/first-name
                :account/last-name
                :account/email]
          :opt [:account/phone]))

(s/valid? :account/person
          {:account/first-name "Bugs"
           :account/last-name "Bunny"
           :account/email "bugs@example.com"})

(s/explain-data :account/person
                {:account/first-name "Bugs"
                 :account/email "n/a"})

;; Мыпа с неквалифицированными ключами.
(s/def :unqualified/person
  (s/keys :req-un [:account/first-name
                   :account/last-name
                   :account/email]
          :opt-un [:account/phone]))

(s/conform :unqualified/person
           {:first-name "Bugs"
            :last-name "Bunny"
            :email "bugs@example.com"
            :phone "+7 999 999 99 99"})

;; s/keys спеки можно применять к записям.
(defrecord Person [first-name last-name email phone])

(s/explain-data :unqualified/person
                (->Person "Bugs" nil nil nil))

(s/conform :unqualified/person
           (->Person "Bugs" "Bunny" "bugs@example.com" "+7 999 999 99 99"))

;; В s/keys мы можем использовать имена спек, которые не были определены.
;; В таком случа проверка будет осуществляться,
;; как если бы была определена спека any?.
(s/def :example/map
  (s/keys :req-un [:example/be-aware]))

(s/valid? :example/map {:be-aware true})
(s/valid? :example/map {:be-aware nil})
(s/valid? :example/map {:name "nikita"})

;; s/merge
(s/def :animal/kind string?)
(s/def :animal/says string?)

(s/def :animal/common
  (s/keys :req [:animal/kind
                :animal/says]))

(s/def :dog/tail? boolean?)
(s/def :dog/breed string?)

(s/def :animal/dog
  (s/merge :animal/common
           (s/keys :req [:dog/tail?
                         :dog/breed])))

(s/valid? :animal/dog {:animal/kind "dog"
                       :animal/says "woof"
                       :dog/tail? true
                       :dog/breed "retriever"})

;; ## Multispec

(def multi-spec-tag
  :response/type)

(defmulti response-type
  #'multi-spec-tag)

(defmethod response-type :response/search
  [_]
  (s/keys :req [:response/type
                :response/timestamp
                :search/url]))

(defmethod response-type :response/error
  [_]
  (s/keys :req [:response/type
                :response/timestamp
                :error/message
                :error/code]))

(s/def :response/response
  (s/multi-spec response-type multi-spec-tag))

(s/valid? :response/response
          {:response/type :response/search
           :response/timestamp 1463970123000
           :search/url "https://clojure.org"})

(s/valid? :response/response
          {:response/type :response/error
           :response/timestamp 1463970123000
           :error/message "Invalid host"
           :error/code 500})

(s/valid? :response/response
          {:response/type :response/restart})
(s/explain-data :response/response
                {:response/type :response/restart})

(s/explain-data :response/response
                {:response/type :response/search
                 :search/url 200})

;; ## Collections

(s/conform (s/coll-of keyword?) [:a :b :c])
(s/conform (s/coll-of number?) #{5 10 2})


(s/def :ex/vnum3
  (s/coll-of number?
             :kind vector?
             :count 3
             :distinct true
             :into #{}))

(s/conform :ex/vnum3 [1 2 3])
;; not a vector
(s/conform :ex/vnum3 #{1 2 3})  
;; not distinct
(s/conform :ex/vnum3 [1 1 1])   
;; not a number
(s/conform :ex/vnum3 [1 2 :a])

(s/def :game/scores (s/map-of string? int?))
(s/conform :game/scores {"Sally" 1000, "Joe" 500})

(s/def :geom/point-tuple
  (s/tuple double? double? double?))

(s/conform :geom/point-tuple [1.5 2.5 -0.5])

(s/def :geom/point-cat
  (s/cat :x double? :y double? :z double?))

(s/conform :geom/point-cat [1.5 2.5 -0.5])

;; s/every и s/every-kv аналогичны s/coll-of и s/map-of, но проверяют
;; только такое количество аргументов, которое указано в *coll-check-limit*.
;; Таким образом можно увеличить производительность наших проверок.
(with-bindings {#'s/*coll-check-limit* 1}
  (s/valid? (s/every int?) [1 2 "3" 4 5]))

;; ## Sequences (Regex ops)

(s/def :cook/ingredient
  (s/cat :quantity number?
         :unit keyword?))

(s/conform :cook/ingredient [2 :teaspoon])

(s/def :ex/seq-of-keywords
  (s/* keyword?))

(s/conform :ex/seq-of-keywords [:a :b :c])
(s/explain-data :ex/seq-of-keywords [10 20])

(s/def :ex/odds-then-maybe-even
  (s/cat :odds (s/+ odd?)
         :even (s/? even?)))

(s/conform :ex/odds-then-maybe-even [1 3 5 100])
(s/conform :ex/odds-then-maybe-even [1])
(s/explain-data :ex/odds-then-maybe-even [100])

(s/def :ex/config
  (s/* (s/cat :prop string?
              :val (s/alt :s string?
                          :b boolean?))))

(s/conform :ex/config ["-server" "foo" "-verbose" true "-user" "joe"])
(s/explain-data :ex/config ['-server "foo"])

(s/def :ex/even-strings
  (s/& (s/* string?) #(even? (count %))))

(s/valid? :ex/even-strings ["a"])
(s/valid? :ex/even-strings ["a" "b"])
(s/valid? :ex/even-strings ["a" "b" "c"])
(s/valid? :ex/even-strings ["a" "b" "c" "d"])

;; When regex ops are combined, they describe a single sequence.
;; If you need to spec a nested sequential collection,
;; you must use an explicit call to spec to start a new nested regex context.
(s/def :ex/unnested
  (s/cat :names-kw #{:names}
         :names (s/* string?)
         :nums-kw #{:nums}
         :nums (s/* number?)))

(s/conform :ex/unnested [:names "a" "b" :nums 1 2 3])

(s/def :ex/nested
  (s/cat :names-kw #{:names}
         :names (s/spec (s/* string?))
         :nums-kw #{:nums}
         :nums (s/spec (s/* number?))))

(s/conform :ex/nested [:names ["a" "b"] :nums [1 2 3]])
