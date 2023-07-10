(defproject web-scraper "0.1.0-SNAPSHOT"
  :description "Simple WEB scrapper example"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.673"]
                 [clj-http "3.12.3"]
                 [org.jsoup/jsoup "1.16.1"]]

  :repl-options {:init-ns async-web-scraper.core})
