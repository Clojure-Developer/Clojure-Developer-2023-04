(ns otus-18.homework.pokemons-test
  (:require
   [cheshire.core :as cheshire]
   [otus-18.homework.pokemons :as subject]
   [clj-http.fake :refer [with-fake-routes]]
   [clojure.test :refer [deftest is]]))

(deftest get-pokemons-test
  (with-fake-routes
    {"https://pokeapi.co/api/v2/pokemon/"
     (fn [_request]
       {:status 200
        :body (cheshire/generate-string
               {:results
                [{:name "pikachu"
                  :url "https://pokeapi.co/api/v2/pokemon/1/"}]})})
     "https://pokeapi.co/api/v2/pokemon/pikachu/"
     (fn [_request]
       {:status 200
        :body (cheshire/generate-string
               {:name "pikachu"
                :types [{:slot 1
                         :type {:name "electric",
                                :url "https://pokeapi.co/api/v2/type/13/"}}]})})
     "https://pokeapi.co/api/v2/pokemon/1/"
     (fn [_request]
       {:status 200
        :body (cheshire/generate-string
               {:name "pikachu"
                :types [{:slot 1
                         :type {:name "electric",
                                :url "https://pokeapi.co/api/v2/type/13/"}}]})})
     "https://pokeapi.co/api/v2/type/13/"
     (fn [_request]
       {:status 200
        :body (cheshire/generate-string
               {:names {:language {:name "ja",
                                   :url "https://pokeapi.co/api/v2/language/11/"},
                        :name "でんき"},})})
     "https://pokeapi.co/api/v2/type/electric/"
     (fn [_request]
       {:status 200
        :body (cheshire/generate-string
               {:names {:language {:name "ja",
                                   :url "https://pokeapi.co/api/v2/language/11/"},
                        :name "でんき"},})})}

    (is (= {"pikachu" ["でんき"]}
           (first (subject/get-pokemons :lang "ja"))))))
