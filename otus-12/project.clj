(defproject otus-12 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[camel-snake-kebab "0.4.3"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/test.check "1.1.1"]]
  :main ^:skip-aot otus-12.core
  :target-path "target/%s"
  :profiles {:dev
             {:jvm-opts ["-Ddev=true"
                         "-Dclojure.spec.check-asserts=true"]}

             :uberjar
             {:aot :all
              :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
