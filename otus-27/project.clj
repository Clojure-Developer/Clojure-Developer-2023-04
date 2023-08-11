(defproject otus-27 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [com.h2database/h2 "1.4.199"]
                 [com.github.seancorfield/honeysql "2.4.1045"]]
  :repl-options {:init-ns otus-27.core})
