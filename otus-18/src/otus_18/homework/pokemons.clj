(ns otus-18.homework.pokemons
  (:require
   [clojure.core.async
    :as async
    :refer [chan <! >! <!! go pipeline-async close! thread]]
   [clj-http.client :as http]))

(def base-url     "https://pokeapi.co/api/v2")
(def pokemons-url (str base-url "/pokemon"))
(def type-path    (str base-url "/type"))

(defn async-request [opts]
  (thread (http/request opts)))

(defn pokemons-request
  [& {:keys [lang limit]}]
  (async-request {:url          pokemons-url
                  :method       :get
                  :as           :json
                  :query-params {:limit limit :lang lang}}))

(defn pokemon-request
  [& {:keys [name lang]}]
  (async-request {:url          (str pokemons-url name)
                  :method       :get
                  :as           :json
                  :query-params {:lang lang}}))

(defn type-request
  [& {:keys [type]}]
  (async-request {:url          (str type-path type)
                  :method       :get
                  :as           :json}))

(defn extract-pokemon-name [pokemon]
  (:name pokemon))

(defn extract-pokemon-types [pokemon]
  (->> (:types pokemon)
       (map (fn [type] (-> type
                           (:type)
                           (:name))))))

(defn extract-type-name [pokemon-type lang]
  (->> (:names pokemon-type)
       (filter (fn [type-name] (= lang (-> type-name :language :name))))
       (first)
       :name))

(defn build-type-names-fetcher []
  (let [cache (atom {})]
    (fn [types lang]
      (let [result> (chan)]
        (go
         (doseq [type types]
           (let [cached (get-in @cache [type lang])]
             (if cached
               (>! result> cached)
               (let [type-result (:body (<! (type-request :type type)))
                     type-name   (extract-type-name type-result lang)]
                 (swap! cache assoc-in [type lang] type-name)
                 (>! result> type-name)))))
         (close! result>))
        result>))))

(defn get-pokemons
  "Асинхронно запрашивает список покемонов и название типов в заданном языке. Возвращает map, где ключами являются
  имена покемонов (на английском английский), а значения - коллекция названий типов на заданном языке."
  [& {:keys [limit lang] :or {limit 50 lang "ja"}}]
  (let [pokemons>   (chan)
        types-fetcher (build-type-names-fetcher)
        pokemons->types> (chan 8)]

    (pipeline-async 8 pokemons->types>
                    (fn [pokemon-name result>]
                      (go (let [pokemon-result (<! (pokemon-request :name pokemon-name :lang lang))
                                pokemon (:body pokemon-result)
                                pokemon-types (extract-pokemon-types pokemon)
                                pokemon-name (extract-pokemon-name pokemon)
                                type-names   (<! (async/into [] (types-fetcher pokemon-types lang)))]

                            (>! result> [pokemon-name type-names]))
                          (close! result>)))
                    pokemons>)

    (go (let [pokemons-result (<! (pokemons-request :lang lang :limit limit))
              pokemon-names     (->> pokemons-result
                                     :body
                                     :results
                                     (map :name))]
          (doseq [pokemon-name pokemon-names]
            (>! pokemons> pokemon-name))
          (close! pokemons>)))

    (<!! (async/into {} pokemons->types>))))
