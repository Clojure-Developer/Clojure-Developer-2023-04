(defproject spec-sampler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [cheshire "5.11.0"]
                 [ring/ring-core "1.10.0"]
                 [ring/ring-devel "1.10.0"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring/ring-defaults "0.3.4"]
                 [compojure "1.7.0"]
                 [hiccup "1.0.5"]
                 [org.clojure/test.check "1.1.1"]]
                 
  :main ^:skip-aot spec-sampler.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
