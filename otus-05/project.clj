(defproject otus-05 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [example "0.1.0"]] ;; checkouts/example
  :main ^:skip-aot otus-05.core
  ;; все ns, классы из которых мы хотим импортировать, нужно упомянуть здесь
  :aot [otus-05.rgb]
  ;; это нужно, чтобы подключить .java-файлы
  :java-source-paths ["java"])
