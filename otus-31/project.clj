(defproject otus-31 "0.1.0-SNAPSHOT"
  :description "Learning goes fast"
  :jvm-opts ["-Djdk.attach.allowAttachSelf"]
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.datomic/peer "1.0.6735"]
                 [criterium/criterium "0.4.6"]
                 [com.clojure-goes-fast/clj-async-profiler "1.0.5"]
                 [com.clojure-goes-fast/clj-java-decompiler "0.1.0"]
                 [datascript/datascript "1.4.2"]])
