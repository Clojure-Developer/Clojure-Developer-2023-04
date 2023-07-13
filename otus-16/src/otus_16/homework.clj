(ns otus-16.homework
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.core.reducers :as r]))

(def log-regex
  #"^([\d+.]+) (\S+) (\S+) (\[[\w+/]+:[\w+:]+ \+\d+\]) \"(.+?)\" (\d{3}) (\d+) \"([^\"]+)\" \"(.+?)\"")

(defn parse-log
  [^String log]
  (let [[_ ip _ user-name date-time request response size referer user-agent] (re-find log-regex log)]
    {:ip ip
     :user-name user-name
     :date-time date-time
     :request request
     :response response
     :size size
     :referer referer
     :user-agent user-agent}))

(defn get-logs-paths
  [dir-path]
  (->> (io/as-file dir-path)
       (file-seq)
       (filter #(.isFile %))))

(defn read-logs
  [logs]
  (map #(io/reader %) logs))

(defn ->url [log]
  {:pre [(contains? log :request)]}
  (let [request (-> log :request)]
    (when request
      (-> request
         (str/split #" ")
         second))))

(defn filter-by-url
  [url logs]
  (if (= url :all)
    logs
    (r/filter #(= url (->url %)) logs)))

(comment
  (r/fold str (filter-by-url "test" [{:request "test"} {:request "test2"} {:request "test"}])))

(defn ->referer [log]
  {:pre [(contains? log :referer)]}
  (-> log :referer))

(defn filter-by-referer
  [referer logs]
  (if (= referer :all)
    logs
    (r/filter #(= referer (->referer %)) logs)))

(defn close-logs
  [logs]
  (map #(.close %) logs))

(defn parse-int-safe
  [^String s]
  (try
    (Integer/parseInt s)
    (catch Exception _
      0)))

(defn sum-size
  [log-file]
  (->> log-file
       (r/map #(parse-int-safe (:size %)))
       (r/fold +)))

(defn process-partition
  [logs {:keys [url referrer]}]
  (->> logs
       (r/map parse-log)
       (filter-by-url url)
       (filter-by-referer referrer)))

(defn sum-partition
  [partition]
  (reduce #(+ %1 (sum-size %2))
          0
          partition))

(defn process-log
  [log-file filter-params]
  (->> log-file
       (line-seq)
       (partition-all 5000)
       (pmap #(process-partition % filter-params))
       (sum-partition)))

(defn solution
  [& {:keys [url referrer]
      :or   {url :all referrer :all}}]
  (let [logs (-> "./logs"
                 get-logs-paths
                 read-logs)]
    (->> logs
         (pmap #(process-log % {:url url :referrer referrer}))
         (reduce +)
         (println "Bytes:"))
    (close-logs logs)))

(comment
 ;; возможные вызовы функции
  (time (solution))
  (time (solution :url "/rss/"))
  (solution :referrer "some-referrer")
  (solution :url "some-url" :referrer "some-referrer"))
