(ns otus-22.core
  (:require [malli.core :as m]
            [malli.swagger :as swagger]
            [malli.transform :as mt]
            [malli.dot :as dot]
            [camel-snake-kebab.core :as csk]
            [malli.util :as mu])
  (:gen-class))

;; type
;; [type & children]
;; [type properties & children]

(m/validate :int 13)
(m/validate :string "13")

(m/validate [:vector int?] [13])
(m/validate [:set int?] [13])

(m/validate int? 13)
(m/validate string? 13)

(m/validate [:string {:min 3}] "13")
(m/validate [:string {:min 3}] "133")
(m/validate [string? {:min 3}] "133")


(def account-schema
  [:map
   [:id integer?]
   [:username string?]
   [:password string?]
   [:email string?]
   [:created-on inst?]])


(m/validate account-schema
            {:id 1
             :username "username"
             :password "password"
             :email "email"
             :created-on #inst "2020"})

(m/validate account-schema
            {:id 12})

(m/decode inst?
          "2020-01-01T00:00:00.000-00:00"
          mt/json-transformer)

(m/encode inst?
          #inst "2020-01-01T00:00:00.000-00:00"
          mt/json-transformer)

(def strict-transformer
  (mt/transformer (mt/key-transformer {:encode csk/->camelCaseString
                                       :decode csk/->kebab-case-keyword})
                  mt/strip-extra-keys-transformer
                  mt/json-transformer))

(m/decode account-schema
          {"id" 1
           "username" "username"
           "password" "password"
           "email" "email"
           "createdOn" "2020-01-01T00:00:00.000-00:00"
           "extraKey1" 1
           "extraKey2" 2}
          strict-transformer)

;; "{\"key\": \"value\"}" => {"key" "value"}

(-> [:map
     [:x int?]
     [:y int?]]
    (update 1 (fn [[k v]]
                [k {:optional true} v]))
    (update 2 (fn [[k v]]
                [k {:optional true} v])))

(mu/optional-keys [:map
                   [:x int?]
                   [:y int?]])

(m/parse
 [:* [:catn
      [:prop string?]
      [:val [:altn
             [:s string?]
             [:b boolean?]]]]]
 ["-server" "foo" "-verbose" true "-user" "joe"])

(def InnerAddress
  [:map
   [:street string?]
   [:city string?]
   [:zip int?]
   [:lonlat [:tuple double? double?]]])

(def Address
  [:map
   [:id string?]
   [:tags [:set keyword?]]
   [:address InnerAddress]])

(swagger/transform Address)

(let [dot-graph (dot/transform Address)]
  (spit "graph.dot" dot-graph)
  (println dot-graph))

(def Order
  [:schema
   {:registry {"Country" [:map
                          [:name [:enum :FI :PO]]
                          [:neighbors [:vector [:ref "Country"]]]]
               "Burger" [:map
                         [:name string?]
                         [:description {:optional true} string?]
                         [:origin [:maybe "Country"]]
                         [:price pos-int?]]
               "OrderLine" [:map
                            [:burger "Burger"]
                            [:amount int?]]
               "Order" [:map
                        [:lines [:vector "OrderLine"]]
                        [:delivery [:map
                                    [:delivered boolean?]
                                    [:address [:map
                                               [:street string?]
                                               [:zip int?]
                                               [:country "Country"]]]]]]}}
   "Order"])

(let [dot-graph (dot/transform Order)]
  (spit "graph.dot" dot-graph)
  (println dot-graph))

(m/walk Address
        (m/schema-walker
         #(mu/update-properties % assoc :title (name (m/type %)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
