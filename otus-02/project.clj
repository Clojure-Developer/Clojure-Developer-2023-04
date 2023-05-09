(defproject otus-02 "0.1.1-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :uberjar-name "my-app.jar"

  :jvm-opts ["-Xmx1g"]

  :main ^:skip-aot otus-02.core

  :resource-paths ["resources"]

  :dependencies [[org.clojure/clojure "1.11.1"]]

  :repl-options {:init-ns otus-02.core}

  :profiles {:uberjar {:aot :all}

             :dev     {:source-paths   ["dev/src"]
                       :resource-paths ["dev/resources"]
                       :dependencies   [[djblue/portal "0.30.0"]]}}

  :aliases {"help" ["run" "help"]})
