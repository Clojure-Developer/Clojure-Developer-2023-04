(ns otus-18.homework.pokemons)

(def base-url     "https://pokeapi.co/api/v2")
(def pokemons-url (str base-url "/pokemon"))
(def type-path    (str base-url "/type"))

(defn extract-pokemon-name [pokemon]
  (:name pokemon))

(defn extract-type-name [pokemon-type lang]
  (->> (:names pokemon-type)
       (filter (fn [type-name] (= lang (-> type-name :language :name))))
       (first)
       :name))

(defn get-pokemons
  "Асинхронно запрашивает список покемонов и название типов в заданном языке. Возвращает map, где ключами являются
  имена покемонов (на английском английский), а значения - коллекция названий типов на заданном языке."
  [& {:keys [limit lang] :or {limit 50 lang "ja"}}])
