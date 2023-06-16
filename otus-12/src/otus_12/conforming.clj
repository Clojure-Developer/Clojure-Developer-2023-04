(ns otus-12.conforming
  (:require [camel-snake-kebab.core :as csk]
            [clojure.data.json :as json]
            [clojure.instant :refer [read-instant-date]]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [otus-12.util :as u]))

;;
;; # Приведение
;;

(s/def ::->int
  (s/conformer (fn [value]
                 (try (Long/valueOf value)
                      (catch Exception _
                        ::s/invalid)))
               str))

(s/def ::int
  (s/or :int int?
        :string ::->int))

(s/conform ::int 2019)
(s/conform ::int "2019")

(->> "2019"
     (s/conform ::int)
     (s/unform ::int))

(s/def ::->date
  (s/conformer (fn [value]
                 (try (read-instant-date value)
                      (catch Exception _
                        ::s/invalid)))
               (fn [^java.util.Date x]
                 (str (.toInstant x)))))

(s/def ::date
  (s/or :date (partial instance? java.util.Date)
        :string ::->date))

(s/conform ::date #inst "2019")
(s/conform ::date "2019")

(->> "2019"
     (s/conform ::date)
     (s/unform ::date))

;; Можно заметить повторяющиеся паттерны. Давайте введём ->conformer

(s/def ::->int
  (u/->conformer (fn [value]
                   (Long/valueOf value))))

(s/def ::int
  (s/or :int int?
        :string ::->int))

(s/def ::->date
  (u/->conformer read-instant-date))

(s/def ::date
  (s/or :date (partial instance? java.util.Date)
        :string ::->date))

;; Конформеры можно объединять.

(s/def ::->boolean
  (s/and (u/->conformer string/lower-case)
         (u/->conformer (fn [value]
                          (case value
                            ("true" "1" "on" "yes") true
                            ("false" "0" "off" "no") false
                            ;; Можно опустить, потмоу что case кидает исключение,
                            ;; когда ни одно значение не подошло.
                            ::s/invalid)))))

(s/def ::boolean
  (s/or :boolean boolean?
        :string ::->boolean))

(s/conform ::boolean false)
(s/conform ::boolean "TRUE")
(s/conform ::boolean "true2")

;; Парсинг с использованием conform и unform. См. parse-spec в util.

(u/parse-spec ::int "2019")
(u/parse-spec ::date "2019")
(u/parse-spec ::boolean "TRUE")


(s/def ::exec-time ::date)
(s/def ::port ::int)

(s/def ::config
  (s/keys :req-un [::exec-time
                   ::port]))

(let [json-config "{\"execTime\": \"2022-01-01\", \"port\": \"8080\"}"
      raw-config (json/read-str json-config :key-fn csk/->kebab-case-keyword)]
  (s/conform ::config raw-config)
  (u/parse-spec ::config raw-config))

;; Если обойтись без s/or, то parse-spec нам не понадобится.
(s/def ::exec-time ::->date)
(s/def ::port ::->int)


(comment
  (if-let [value true]
    value
    ::s/invalid)
  
  (if-let [value true]
    value
    '::s/invalid)
  
  (s/valid? any? ::s/invalid)
  )
