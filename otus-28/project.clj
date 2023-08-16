(defproject otus-28 "0.1.0-SNAPSHOT"

  :description "OTUS Clojure Developer #28"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.apache.kafka/kafka-clients "3.3.2"]
                 [org.apache.kafka/kafka-streams "3.3.2"]
                 [org.apache.kafka/kafka-streams-test-utils "3.3.2"]


                 [fundingcircle/jackdaw "0.9.11"]]

  :repositories [["confluent" {:url "https://packages.confluent.io/maven/"}]]

  :repl-options {:init-ns otus-28.core})
