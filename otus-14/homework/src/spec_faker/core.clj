(ns spec-faker.core
  (:require [clojure.string :as str]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.resource :refer [wrap-resource]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer [html]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [redirect]]
            [ring.swagger.swagger-ui :refer [wrap-swagger-ui]]
            [ring.util.codec :refer [url-encode]]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.test.check.generators :as gen*]
            [cheshire.core :refer [parse-string generate-string]])
  (:gen-class))

(defn page [title & body]
  [:html
   [:head
    [:title title]
    [:link {:rel "stylesheet" :href "css/style.css"}]]
   [:body
    body]])

(defn index-page []
  (html
    (page
       "Data generator by clojure.spec"
       [:h1 "Data generator by clojure.spec"]
       [:h3 "Submit your clojure.spec as a json"]
       [:form {:method "POST"}
        [:div
         [:textarea {:name "spec"}]]
        [:div
         [:div
          [:a {:href "swagger/index.html#/api/post_generate"} "Read swagger documentation"]]
         [:button {:type "submit"} "Send"]]])))

(s/def ::int int?)
(gen/generate (s/gen ::int))

(s/def ::string string?)
(gen/generate (s/gen ::string))

(s/def ::number number?)
(gen/generate (s/gen ::number))

(s/def ::float float?)
(gen/generate (s/gen ::float))

(s/def ::double double?)
(gen/generate (s/gen ::double))

(defn generate-date
  [format]
  (let [from 1577836800 ; 2020 year
        to (int (/ (System/currentTimeMillis) 1000))
        ts (-> (gen*/large-integer* {:min from :max to})
               (gen/generate))
        dt (java.util.Date. (* ts 1000))]
    (.format (java.text.SimpleDateFormat. format) dt)))

(def generators
  {:number        #(gen/generate (s/gen ::int))
   :number-float  #(gen/generate (s/gen ::float))
   :number-double #(gen/generate (s/gen ::double))
   :integer       #(gen/generate (s/gen ::int))
   :integer-int32 #(gen/generate (s/gen ::int))
   :integer-int64 #(gen/generate gen*/large-integer)
   :string        #(gen/generate (s/gen ::string))
   :string-date   #(generate-date "yyyy-MM-dd")
   :string-date-time
   (fn []
     (let [dt (generate-date "yyyy-MM-dd HH:mm:ss")
           [d t] (str/split dt #" ")]
       (str d "T" t "Z")))})

(defn get-generator
  [type format]
  (let [gen-key-str (str/join "-" (filter (complement nil?) [type format]))
        gen-key (keyword gen-key-str)
        generator (get generators gen-key)]
    generator))

(defn generate
  [spec]
  (into {} (map (fn [[n {:keys [type format]}]] [n ((get-generator type format))]) spec)))

(defn valid-field-spec?
  [{:keys [type format]}]
  (let [gen-key-str (str/join "-" (filter (complement nil?) [type format]))
        gen-key (keyword gen-key-str)
        generator (get generators gen-key)]
    (not (nil? generator))))

(defn validate-spec
  [spec]
  (let [invalid
          (filter
            (fn [[_ s]]
              ((complement valid-field-spec?) s)) spec)
        result
          (into {} (map
                     (fn [[n s]]
                       (let [{t :type f :format} s
                             error (cond
                                     (and (some? t) (some? f)) (format "unsupported for type = %s and format = %s" t f)
                                     (some? t) (format "unsupported for type = %s" t)
                                     (some? f) (format "missing type for format = %s" f)
                                     :else (format "missing type"))]
                            [n error])) invalid))]
    (when-not (empty? result) result)))

(defn generate-by-json-spec
  ([json-spec]
   (generate-by-json-spec json-spec {}))

  ([json-spec opt-map]
   (when-let [spec (parse-string json-spec true)]
     (let [errors (validate-spec spec)]
       (if (nil? errors)
         (let [data (generate spec)
               json-data (generate-string data opt-map)]
           {:ok? true :data json-data})
         (let [json-errors (generate-string errors opt-map)]
           {:ok? false :data json-errors}))))))

(defn generate-handler
  [json-spec]
  (let [{:keys [ok? data]} (generate-by-json-spec json-spec)]
    (if ok? {:status 200
             :headers {"Content-Type" "application/json"}
             :body data}
            {:status 400
             :headers {"Content-Type" "application/json"}
             :body data})))

(defroutes router
  (routes
    (GET "/" [& {spec :spec}]
      (if (nil? spec)
        (index-page)
        (html (page "Generation result"
                 [:h1 "Generation result"]
                 [:pre (:data (generate-by-json-spec spec {:pretty true}))]
                 [:a {:href "/"} "← Go Back to form"])))))

    (POST "/" [& {spec :spec}]
      (redirect (str "/?spec=" (url-encode spec))))

    (POST "/generate" {body :body}
      (generate-handler (slurp body)))

    (GET "/swagger.json" []
      (redirect "/swagger/swagger.json"))

    (route/not-found
      (html
        (page "Page not found"
              [:h1 "Oops!"]))))

(def app
  (-> #'router
      wrap-keyword-params
      wrap-params
      (wrap-resource "public")
      (wrap-swagger-ui {:path         "/swagger"
                        :root         "swagger-ui"
                        :swagger-docs "swagger.json"})))

(comment
  (run-jetty #'app {:join? false
                    :port 8000}))

(defn -main
  [& args]
  (run-jetty
   app
   {:port 8000}))
