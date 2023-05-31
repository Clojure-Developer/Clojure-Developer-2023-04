(defproject api-example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[aero "1.1.6"]
                 [camel-snake-kebab "0.4.3"]
                 [com.github.seancorfield/honeysql "2.4.1033"]
                 [com.github.seancorfield/next.jdbc "1.3.874"]
                 [compojure "1.7.0"]
                 [integrant "0.8.0"]
                 [migratus "1.4.9"]
                 [nubank/matcher-combinators "3.8.5"]
                 [org.clojure/clojure "1.11.1"]
                 [org.postgresql/postgresql "42.6.0"]
                 [org.slf4j/slf4j-log4j12 "2.0.7"] ; Required by migratus.
                 [ring "1.10.0"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-mock "0.4.0"]]
  :main ^:skip-aot api-example.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  
  :test-selectors {:integration :integration
                   :unit (complement :integration)
                   :crud (fn [m]
                           (->> (:name m)
                                (name)
                                (re-find #"get|create|delete|update|replace")))})
