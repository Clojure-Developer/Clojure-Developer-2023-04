(ns otus-12.instrumentation
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as st]
            [otus-12.util :as u]))

(defn person-name
  [person]
  {:pre [(s/valid? :account/person person)]
   :post [(s/valid? string? %)]}
  (let [{:account/keys [first-name last-name]} person]
    (str first-name " " last-name)))

(person-name 42)

(person-name {:account/first-name "Bugs"
              :account/last-name "Bunny"
              :account/email "bugs@example.com"})

(defn person-name
  [person]
  (s/assert :account/person person)
  (let [{:account/keys [first-name last-name]} person]
    (str first-name " " last-name)))

(comment
  ;; Для того, чтобы s/assert заработал, необходимо изменить значение
  ;; опции clojure.spec.check-asserts в true в project.clj:
  :profiles {:dev {:jvm-opts ["-Dclojure.spec.check-asserts=true"]}}
  ;; либо
  (s/check-asserts true)
  )

(s/check-asserts true)

(person-name 100)

(defn- set-config [prop val]
  ;; dummy fn
  (println "set" prop val))

(defn configure [input]
  (let [parsed (s/conform :ex/config input)]
    (if (s/invalid? parsed)
      (throw (ex-info "Invalid input" (s/explain-data :ex/config input)))
      (for [{prop :prop [_ val] :val} parsed]
        (set-config (subs prop 1) val)))))

(configure ["-server" "foo"
            "-verbose" true
            "-user" "joe"])

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
  :fn (fn [{conformed-args :args conformed-ret :ret}]
        (and (<= (:start conformed-args) conformed-ret)
             (< conformed-ret (:end conformed-args)))))

(defn ranged-rand
  "Returns random int in range start <= rand < end"
  [start end]
  (+ start (long (rand (- end start)))))

;; st/instrument ожидает имена функций в виде символов вместе с пространством
;; имён. Поэтому я использую syntax-quote (`).
(st/instrument `ranged-rand)

;; Либо можно воспользоваться моим вспомогательным макросом.
(u/instrument-ns)

(ranged-rand 1 10)


;; s/keys* спеки для проверки кваргов.
(comment
  ;; clojure map destructuring can be applied to ISeq collections
  ;; (seq? x) => true
  (let [{:keys [b c]} '(:b 2 :c 3)]
    [b c])

  (defn my-func
    [a & {:keys [b c]}]
    [a b c])
  (my-func 1 :b 2 :c 3)
  (my-func 1 {:b 2 :c 3})

  (let [{:keys [b c]} (rest [1 :b 2 :c 3])]
    [b c])

  (let [{:keys [b c]} (rest [1 {:b 2 :c 3}])]
    [b c]))

(s/def :my.config/id keyword?)
(s/def :my.config/host string?)
(s/def :my.config/port number?)

(s/def :my.config/server
  (s/keys* :req [:my.config/id
                 :my.config/host]
           :opt [:my.config/port]))

(s/conform :my.config/server [:my.config/id :s1
                              :my.config/host "example.com"
                              :my.config/port 5555])

(s/def ::c int?)

(s/fdef sum-args
  :args (s/cat :a int?
               :b int?
               :kwargs (s/keys* :opt-un [::c]))
  :ret int?)

(defn sum-args
  [a b & {:keys [c]
          :or {c 0}}]
  (+ a b c))

(st/instrument `sum-args)

(sum-args 1 2 :c 4)
(sum-args 1 2 {:c 4})
(sum-args 1 2 {:c "4"})

(s/fdef adder
  :args (s/cat :x number?)
  :ret (s/fspec :args (s/cat :y number?)
                :ret number?)
  :fn (fn [{:keys [args ret]}]
        (= (:x args) (ret 0))))

(comment
  ;; s/fdef можно заменить на s/def + fn-symbol (без quote) + fspec
  (s/def adder
    (s/fspec :args (s/cat :x number?)
             :ret fn?))
  )

(->> (s/registry)
     (keys)
     (filter (comp (partial re-find #"adder")
                   str)))

(defn adder [x]
  (fn [y]
    (+ x y)))

(st/instrument `adder)

(adder 4)

;; sdkw удобно использовать для определения спек для аргументов и/или кваргов
;; с одинаковыми именами внутри одного файла, но использующимися в разных функциях.
(s/def #sdkw :foo-bar-func/foo any?)
(s/def #sdkw :foo-bar-func/bar any?)

(s/fdef foo-bar-func
  :args (s/cat :foo #sdkw :foo-bar-func/foo
               :bar #sdkw :foo-bar-func/bar))

(defn foo-bar-func
  [foo bar]
  [foo bar])

(st/instrument `foo-bar-func)

(foo-bar-func 1 2)

;; Достаточно поместить в конец файла и один раз целиком вычислить пространство имён.
(u/instrument-ns)
