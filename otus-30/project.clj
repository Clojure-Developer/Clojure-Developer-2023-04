(defproject otus-30 "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.11.1"]

                 [org.slf4j/slf4j-api "1.7.32"]
                 [ch.qos.logback/logback-classic "1.2.6"]
                 [org.clojure/tools.logging "1.2.4"]

                 [com.brunobonacci/mulog "0.9.0"]

                 [nrepl "0.9.0"]

                 [com.amazonaws/aws-lambda-java-runtime-interface-client "2.4.0"]]

  :uberjar-name "production-app.jar"

  :main ^:skip-aot otus-30.core

  :repl-options {:init-ns otus-30.core}

  :profiles {:uberjar {:aot :all}})
