(defproject mini-redis "0.1.0-SNAPSHOT"
  :description "Redis (ligth version of it) implemented in Clojure"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.673"]]

  :repl-options {:init-ns mini-redis.core})
