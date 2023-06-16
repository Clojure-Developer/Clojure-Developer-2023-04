(ns otus-12.testing
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.spec.test.alpha :as st]))

(gen/generate (s/gen int?))

(gen/generate (s/gen nil?))

(gen/sample (s/gen string?))

(gen/sample (s/gen #{:club :diamond :heart :spade}))

(gen/sample (s/gen (s/cat :k keyword? :ns (s/+ number?))))

;; Возвращает пары из сгененерированных и законформленных значений
(s/exercise (s/cat :k keyword? :ns (s/+ number?)))

(s/exercise-fn 'otus-12.instrumentation/ranged-rand)

;; Не на все предикаты определены генераторы.
(gen/generate (s/gen even?))

;; Решить проблему можно использую s/and и предоставив предикат,
;; для которого существует генератор. Последующие предикаты будут выполнять
;; роль филтра.
(gen/generate (s/gen (s/and int? even?)))

(defn divisible-by
  [n]
  (fn [x]
    (zero? (mod x n))))

(gen/sample (s/gen (s/and int?
                          pos?
                          (divisible-by 3))))

;; За 100 попыток не удалось сгенерировать подходящего значения.
(gen/sample (s/gen (s/and string?
                          (partial re-find #"hello"))))

;;
;; Кастомные генераторы
;;

(s/def :ex/kws (s/and keyword? #(= (namespace %) "my.domain")))
(s/valid? :ex/kws :my.domain/name)
(gen/sample (s/gen :ex/kws))

(def kw-gen
  (s/gen #{:my.domain/name
           :my.domain/occupation
           :my.domain/id}))

(gen/sample kw-gen 5)

(s/def :ex/kws
  (s/with-gen (s/and keyword? #(= (namespace %) "my.domain"))
    (fn []
      kw-gen)))

(s/valid? :ex/kws :my.domain/name)
(gen/sample (s/gen :ex/kws))

(def kw-gen-2
  (gen/fmap (partial keyword "my.domain")
            (gen/string-alphanumeric)))

;; Многие генераторы по своему дизайну возвращают более простые значения
;; в начале.
(gen/sample kw-gen-2)

(def kw-gen-3
  (->> (gen/string-alphanumeric)
       (gen/such-that (partial not= ""))
       (gen/fmap (partial keyword "my.domain"))))

;; Отфильтровываем простые значения с помощью gen/such-that.
(gen/sample kw-gen-3)

(s/def :ex/hello
  (s/with-gen (partial re-find #"hello")
    (fn []
      (gen/fmap (fn [[s1 s2]]
                  (str s1 "hello" s2))
                (gen/tuple (gen/string-alphanumeric)
                           (gen/string-alphanumeric))))))

;; Больше информации можно найти в пространстве имён
;; clojure.test.check.generators
;; библиотеки org.clojure/test.check

(gen/sample (s/gen :ex/hello))

(s/def :bowling/roll
  (s/int-in 0 11))

(gen/sample (s/gen :bowling/roll))

(s/def :ex/the-aughts
  (s/inst-in #inst "2000" #inst "2010"))

(drop 50 (gen/sample (s/gen :ex/the-aughts) 55))

(s/def :ex/dubs
  (s/double-in :min -100.0 :max 100.0 :NaN? false :infinite? false))

(s/valid? :ex/dubs 2.9)
(s/valid? :ex/dubs Double/POSITIVE_INFINITY)
(gen/sample (s/gen :ex/dubs))

;;
;; Тестирование
;;

;; Несколько раз нужно запустить и можно увидеть ошибку,
;; когда (- end start) будет больше чем Long/MAX_VALUE
(st/check 'otus-12.instrumentation/ranged-rand)

(s/fdef ranged-rand
  :args (s/& (s/cat :start int?
                    :end int?)
             ;; Обратите внимание, что переданное значение является
             ;; результатом применения (s/conform (s/cat ...) ...).
             ;; Таким образом можно комбинировать конформеры.
             (fn [conformed-value]
               (< (:start conformed-value) (:end conformed-value))))
  :ret int?
  ;; Для работы :fn должны быть определены :ret и :args.
  ;; В :fn будут передаваться мапа с законформленными ret и args:
  ;; {:args conformed-args :ret conformed-ret}
  :fn (s/and (fn [{:keys [args ret]}]
               (println args ret)
               (and (<= (:start args) ret)
                    (< ret (:end args))))
             (constantly true)))

(defn ranged-rand
  "Returns random int in range start <= rand < end"
  [end start]
  (+ start (long (rand (- end start)))))

(st/instrument `ranged-rand)

(st/abbrev-result (first (st/check `ranged-rand)))

#_(-> (st/enumerate-namespace 'user)
      (st/check))

;;
;; Инструмментация + генерация
;;

(s/def :svc/query string?)
(s/def :svc/request (s/keys :req [:svc/query]))
(s/def :svc/result (s/coll-of string? :gen-max 3))
(s/def :svc/error int?)

(s/def :svc/response
  (s/or :ok (s/keys :req [:svc/result])
        :err (s/keys :req [:svc/error])))

(s/fdef invoke-service
  :args (s/cat :service any?
               :request :svc/request)
  :ret :svc/response)

(defn invoke-service
  [service request]
  ;; invokes remote service
  )

(s/fdef run-query
  :args (s/cat :service any?
               :query string?)
  :ret (s/or :ok :svc/result
             :err :svc/error))

(defn run-query
  [service query]
  (let [{:svc/keys [result error]} (invoke-service service {:svc/query query})]
    (or result error)))

(st/instrument [`invoke-service `run-query] {:stub #{`invoke-service}})

(invoke-service nil {:svc/query "test"})
(invoke-service nil {:svc/query "test"})

(st/summarize-results (st/check `run-query))