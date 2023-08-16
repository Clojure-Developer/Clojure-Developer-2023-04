(defproject kafka-web-scraper "0.1.0-SNAPSHOT"

  :description "FIXME: write description"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"]
                 [org.jsoup/jsoup "1.16.1"]
                 [fundingcircle/jackdaw "0.9.11"]
                 [org.apache.kafka/kafka-streams-test-utils "3.3.2"]]

  :repl-options {:init-ns kafka-web-scraper.core})
