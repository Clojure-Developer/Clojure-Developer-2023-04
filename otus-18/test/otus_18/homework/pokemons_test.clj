(ns otus-18.homework.pokemons-test
  (:require
   [cheshire.core :as cheshire]
   [otus-18.homework.pokemons :as subject]
   [clj-http.fake :refer [with-global-fake-routes-in-isolation]]
   [clojure.test :refer [deftest is]]))

(deftest get-pokemons-test
  (with-global-fake-routes-in-isolation
    {{:address "https://pokeapi.co/api/v2/pokemon" :query-params {:limit 50 :lang "ja"}}
     (fn [_request]
       {:status 200
        :body (cheshire/generate-string
               {:results
                [{:name "pikachu"
                  :url "https://pokeapi.co/api/v2/pokemon/1/"}]})})
     {:address "https://pokeapi.co/api/v2/pokemon/pikachu" :query-params {:lang "ja"}}
     (fn [_request]
       {:status 200
        :body (cheshire/generate-string
               {:name "pikachu"
                :types [{:slot 1
                         :type {:name "electric",
                                :url "https://pokeapi.co/api/v2/type/13/"}}]})})
    {:address "https://pokeapi.co/api/v2/pokemon/1/" :query-params {:lang "ja"}}
     (fn [_request]
       {:status 200
        :body (cheshire/generate-string
               {:name "pikachu"
                :types [{:slot 1
                         :type {:name "electric",
                                :url "https://pokeapi.co/api/v2/type/13/"}}]})})
     "https://pokeapi.co/api/v2/type/electric"
     (fn [_request]
       {:status 200
        :body (cheshire/generate-string
               {:names [{:language {:name "ja",
                                    :url  "https://pokeapi.co/api/v2/language/11/"},
                         :name     "でんき"}]})})
    "https://pokeapi.co/api/v2/type/13/"
    (fn [_request]
      {:status 200
       :body (cheshire/generate-string
              {:names {:language {:name "ja",
                                  :url "https://pokeapi.co/api/v2/language/11/"}}})})}

    (is (= {"pikachu" ["でんき"]}
           (subject/get-pokemons :lang "ja")))))
