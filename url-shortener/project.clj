(defproject url-shortener "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/test.check "1.1.1"]
                 [nubank/matcher-combinators "3.8.5"]]
  :repl-options {:init-ns url-shortener.core}

  :main ^:skip-aot url-shortener.core

  :test-selectors {:integration :integration})
