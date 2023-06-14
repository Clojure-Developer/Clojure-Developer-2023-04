(ns otus-12.html
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]))

(def void-elements
  #{:area :base :br :col :command
    :embed :hr :img :input :link
    :meta :keygen :param :source
    :track :wbr})

(s/def ::props (s/map-of keyword? any?))

(s/def ::void-element
  (s/cat :name void-elements
         :props (s/? ::props)))

(s/def ::element
  (s/cat :name (s/and keyword?
                      (complement void-elements))
         :props (s/? ::props)
         :body (s/* ::node)))

(s/def ::node
  (s/or :string string?
        :number number?
        :void-element ::void-element
        :element ::element))

(declare stringify-html)

(defn- stringify-props
  [props]
  (let [kvs (map (fn [[k v]]
                   (str (name k) "=\"" v "\""))
                 props)]
    (when (seq kvs)
      (str " " (string/join " " kvs)))))

(defn- stringify-void-element
  [data]
  (let [tag (name (:name data))
        props (:props data)]
    (str "<" tag (stringify-props props) " />")))

(defn- stringify-element
  [data]
  (let [tag (name (:name data))
        props (:props data)
        content (:body data)]
    (str "<" tag (stringify-props props) ">"
         (string/join "" (map stringify-html content))
         "</" tag ">")))

(defn- stringify-html
  [html-ast]
  (let [[type data] html-ast]
    (case type
      :string data
      :number (str data)
      :void-element (stringify-void-element data)
      :element (stringify-element data))))

(defn hiccup->string
  [hiccup]
  (let [maybe-html-ast (s/conform ::node hiccup)]
    (if (s/invalid? maybe-html-ast)
      (throw (Exception. (s/explain-str ::node hiccup)))
      (stringify-html maybe-html-ast))))

(def some-hiccip
  [:div {:class "mt-4"
         :prop 'value}
   [:input {:type "text"}]
   "some text"
   [:a {:href "http://google.com"}]
   [:div
    [:span
     "a"
     "b"
     "c"]]])

(->> some-hiccip
     (s/conform ::node)
     (s/unform ::node))

(println (hiccup->string some-hiccip))
