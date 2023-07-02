(ns otus-16.homework
  (:gen-class)
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn parse-record
  [record]
  (if-let [[_ request bytes referrer] (re-find #"\"(.*?)\" \d+ (\d+) \"(.*?)\"" record)]
    (let [[_ url-with-params _] (str/split (or request "") #" ")
          [_ url] (re-find #"([^\?]*)" (or url-with-params ""))]
      {:url (or url "") :bytes (or (parse-long bytes) 0) :referrer (or referrer "")})
    {:url "" :bytes 0 :referrer ""}))

(defn calc-total-bytes
  [lines custom-filter]
  (transduce
    (comp
      (map parse-record)
      (filter custom-filter)
      (map :bytes))
    + lines))

(defn process-file
  [file custom-filter]
  (let [lines (line-seq file)]
    (future
      (calc-total-bytes lines custom-filter))))

(defn process-files
  [files custom-filter]
  (map #(process-file % custom-filter) files))

(defn ->custom-filter
  [{:keys [url referrer]}]
  (cond
    (and (some? url) (some? referrer))
    (fn [record] (and (= (:url record) url)
                      (= (:referrer record) referrer)))

    (some? url)
    (fn [record] (= (:url record) url))

    (some? referrer)
    (fn [record] (= (:referrer record) referrer))

    :default (constantly true)))

(defn open-files
  [file-paths]
  (map io/reader file-paths))

(defn close-files
  [files]
  (doseq [f files] (.close f)))

(defmulti solution (fn [files & _] (type (first files))))

(defmethod solution java.lang.String
  [file-paths & rest]
  (let [files (open-files file-paths)]
    (try
      (apply solution files rest)
      (finally close-files files))))

(defmethod solution java.io.Reader
  [files & {:keys [url referrer]
            :or {url nil referrer nil}}]
  (let [custom-filter (->custom-filter {:url url :referrer referrer})]
    (apply + (map deref (process-files files custom-filter)))))

(defn get-file-paths
  [file-prefix numbers]
  (map #(str file-prefix %) numbers))

(defn -main
  [& [file-prefix]]
  (if (nil? file-prefix)
    (do (println "Pass file prefix")
        (System/exit -1))
    (do
      (println "Total bytes of [log.{2-9}]"
               (time (solution (get-file-paths file-prefix (range 2 10)))))
      (shutdown-agents))))
