(ns api-example.test-util
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [api-example.core]
            [api-example.util :as u]
            [next.jdbc :as jdbc]))

(defn fix-factory
  [type index]
  (fn [t]
    (println (format "%s %s starts" type index))
    (t)
    (println (format "%s %s ends" type index))))

(defn fix-with-reference-db
  [t]
  (let [config (u/load-config "config.edn" {:profile :test})
        system (ig/init config [:core/migrator])
        ds (:core/migrator system)]
    (try
      (binding [api-example.core/*ctx* {:ds ds}]
        (t))
      (finally
        (jdbc/with-transaction [tx ds]
          (jdbc/execute! tx ["DROP SCHEMA \"public\" CASCADE"])
          (jdbc/execute! tx ["CREATE SCHEMA \"public\""])
          (jdbc/execute! tx ["GRANT ALL ON SCHEMA \"public\" TO \"postgres\""])
          (jdbc/execute! tx ["GRANT ALL ON SCHEMA \"public\" TO \"public\""]))))))

(defn fix-with-now
  [t]
  (with-redefs [u/now (fn []
                        #inst "0001-01-01")]
    (t)))
