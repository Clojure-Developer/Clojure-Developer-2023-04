(ns url-shortener.main
  (:gen-class)
  (:require
   [duct.core :as duct]
   [url-shortener.web]))

(duct/load-hierarchy)

(defn -main [& args]
  (let [keys     (or (duct/parse-keys args) [:url-shortener.web/server])
        profiles [:duct.profile/prod]]
    (-> (duct/resource "url_shortener/config.edn")
        (duct/read-config)
        (duct/exec-config profiles keys))
    (System/exit 0)))
