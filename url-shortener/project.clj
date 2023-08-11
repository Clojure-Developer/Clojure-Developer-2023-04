(defproject url-shortener "0.1.0-SNAPSHOT"

  :description "URL shortener app"

  :source-paths ["src" "resources"]

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring-core "1.10.0"]
                 [ring/ring-devel "1.10.0"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring/ring-defaults "0.3.4"]
                 [ring/ring-json "0.5.1"]
                 [compojure "1.7.0"]

                 ;; DB
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [com.h2database/h2 "1.4.199"]
                 [com.github.seancorfield/honeysql "2.4.1045"]]

  :profiles {:cljs
             {:dependencies
              [[reagent "1.2.0"]
               [cljs-http "0.1.46"]
               [thheller/shadow-cljs "2.25.2"]]}}

  :repl-options {:init-ns url-shortener.main}

  :main ^:skip-aot url-shortener.main)
