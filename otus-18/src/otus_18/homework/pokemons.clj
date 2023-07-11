(ns otus-18.homework.pokemons
  (:require
    [clj-http.client :as http]
    [cheshire.core :refer [parse-string]]
    [clojure.core.async
     :as async
     :refer [<!! >! <! chan go pipeline-async onto-chan! to-chan! close!]]))

(def base-url     "https://pokeapi.co/api/v2")
(def pokemons-url (str base-url "/pokemon/"))

(defn extract-type-name [pokemon-type lang]
  (->> (:names pokemon-type)
       (filter (fn [type-name] (= lang (-> type-name :language :name))))
       (first)
       :name))

(defn extract-first-type-name [pokemon-type]
  (->> (:names pokemon-type)
       (first)
       :name))

(def get-data
  (memoize (fn [url]
             (-> (http/get url)
                 :body
                 (parse-string true)))))

(defn get-pokemon-type-name
  [url lang]
  (let [type-info (get-data url)]
    (or (extract-type-name type-info lang)
        (extract-type-name type-info "en")
        (extract-first-type-name type-info)
        "unknown")))

(defn load-type-names
  [pokemon-info lang]
  (let [out-ch (chan)
        types-urls-ch (->> (:types pokemon-info)
                           (map #(get-in % [:type :url]))
                           (to-chan!))]
    (pipeline-async 1 out-ch (fn [url result-ch]
                               (go
                                 (let [type-name (get-pokemon-type-name url lang)]
                                   (>! result-ch type-name)
                                   (close! result-ch))))
                    types-urls-ch)
    out-ch))

(defn get-pokemons
  "Асинхронно запрашивает список покемонов и название типов в заданном языке. Возвращает map, где ключами являются
  имена покемонов (на английском английский), а значения - коллекция названий типов на заданном языке."
  [& {:keys [limit lang] :or {limit 50 lang "ja"}}]
  (let [pokemon-urls-ch (chan)
        pokemons-ch (chan)
        out-ch (chan)]

    ; Асинхронно вычитываем инфу по покемонам и асинхронно ходим за типами
    (pipeline-async 1 out-ch
                    (fn [pokemon-info result-ch]
                      (go
                        (let [name (:name pokemon-info)
                              type-names-ch (async/into [] (load-type-names pokemon-info lang))]
                          (>! result-ch [name (<! type-names-ch)])
                          (close! result-ch))))
                    pokemons-ch)

    ; Асинхронно ходим за инфой по покемонам
    (pipeline-async 1 pokemons-ch (fn [url result-ch]
                                    (go
                                      (>! result-ch (get-data url))
                                      (close! result-ch)))
                    pokemon-urls-ch)

    ; Асинхронно раскладываем урлы на покемонов
    (go (let [url (str pokemons-url "?limit=" limit)
              pokemon-url-structures (-> (http/get url)
                                         :body
                                         (parse-string true)
                                         :results)
              pokemons-urls (map :url pokemon-url-structures)]
          (onto-chan! pokemon-urls-ch pokemons-urls)))

    ; Достаем данные из выходного канала в результирующую мапу
    (<!! (async/into {} out-ch))))
