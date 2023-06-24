(ns spec-faker.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hiccup.page :refer [html5]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.swagger.swagger2 :as rs]
            [ring.swagger.json-schema :as rjs]
            [ring.swagger.swagger-ui :refer [wrap-swagger-ui]]
            [hiccup.util :refer [url]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [ring.util.response :refer [redirect]]
            [schema.core :as sch]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [cheshire.core :refer [generate-string parse-string]])
  (:gen-class))

(defn map-vals [f m] (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))

(def type->spec
  {"string" string?
   "integer" int?
   "number" float?
   "boolean" boolean?})

(defn generate-data
  "Generate data from swagger schema string. Returns map with `:ok?` key and either `:data` or `:error` key.
   Works only with plain JSON schema Nested schemas are not supported.
   Ex: `{name: { type: string } id: { type: integer }}` produces `{name: \"string\", id: 1}`"
  [^String schema]
  (try
    (let [schema (parse-string schema true)
          data (map-vals (fn [element] (-> type->spec
                                           (get (:type element))
                                           s/gen
                                           gen/generate)) schema)]
      {:ok? true :data data})
    (catch Exception e
      (println "Error: " (.getMessage e))
      {:ok? false :error (.getMessage e)})))


(comment
  (run-jetty #'app
             {:port 8080
              :join? false}))

(defn page [title & body]
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:script {:src "https://cdn.tailwindcss.com?plugins=forms,typography"}]
    [:title title]]
   [:body
    body]])

(defn spec-page [schema]
  (let [{:keys [ok? data error]} (generate-data schema)]
   (html5 (page "Data Generator"
                [:div {:class "min-h-screen container mx-auto bg-gray-300 p-4"}
                 [:h1 {:class "text-3xl font-bold mb-4"} "Generate data from schema"]
                 [:form
                  {:method "POST" :class "flex flex-col bg-gray-200 shadow-md rounded p-2 mb-4"}
                  [:label {:class "mb-3"} "Spec JSON:"]
                  [:textarea {:name "json" :class "form-textarea mb-3" :placeholder "Enter valid JSON"} schema]
                  (anti-forgery-field)
                  [:button
                   {:type "submit" :class "bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"}
                   "Generate"]]
                 (and schema
                      [:div {:class "container mx-auto mb-4"}
                            (if ok?
                              [:div
                               [:h2 {:class "text-2xl font-bold mb-3"} "Generated data:"]
                               [:pre {:class "bg-gray-200 p-4"} data]]
                              [:div
                               [:h2 {:class "text-2xl font-bold mb-3"} "Error:"]
                               [:pre {:class "bg-red-200 p-4"} error]])])
                 [:footer {:class "text-center text-gray-500 text-xs"} "Homework for Otus © 2023"]]))))

(sch/defschema Spec
  (rjs/field
   {:type (sch/enum "string" "integer" "boolean" "number")}
   {:minProperties 1
     :description "Schema for swagger spec"}))

(sch/defschema Json
  (sch/map-entry sch/Str Spec))

(def router-schema
  {:paths {"/" {:get {:produces ["text/html"]
                      :responses {200 {:description "OK"
                                       :content {"text/html" {:schema {:type "string"}}}}}}
                :post {:produces ["text/html"]
                       :parameters  {:path {:json Json}}
                       :responses {200 {:description "OK"
                                        :content {"text/html" {:schema {:type "string"}}}}}}}}})

(defroutes router
  (GET "/" [schema]
    (spec-page schema))

  (POST "/" [json]
    (redirect (str (url "/" {:schema json}))))

  (GET "/swagger.json" []
    {:headers {"Content-Type" "application/json"}
     :body (-> router-schema
               rs/swagger-json
               sch/with-fn-validation
               generate-string)})
  (route/not-found
   (html5
    (page
     "Page not found"
     [:h1 "Oops!"]))))

(def app
  (-> #'router
      (wrap-swagger-ui {:path "/doc"})
      (wrap-defaults site-defaults)))

(defn -main
  [& args]
  (run-jetty
   app
   {:port (Integer/parseInt (or (first args) "8080"))}))
