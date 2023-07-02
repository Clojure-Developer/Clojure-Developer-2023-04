(ns otus-16.homework
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.core.reducers :as r]))

(defn parse-log
  [^String log]
  (let [regex #"^([\d+.]+) (\S+) (\S+) (\[[\w+/]+:[\w+:]+ \+\d+\]) \"(.+?)\" (\d{3}) (\d+) \"([^\"]+)\" \"(.+?)\""
        [_ ip user-name date-time request response size referer user-agent] (re-find regex log)]
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
      (.listFiles)
      (filter #(.isFile %))
      (map #(.getPath %))))

(defn read-logs
  [logs]
  (map #(io/reader %) logs))

(defn filter-by-url
  [url logs]
  (if (= url :all)
    logs
    (letfn [(->url [log] (-> log :request (str/split #" ") second))]
      (r/filter #(= url (->url %)) logs))))

(defn filter-by-referer
  [referer logs]
  (if (= referer :all)
    logs
    (letfn [(->referer [log] (-> log :referer))]
      (r/filter #(= referer (->referer %)) logs))))

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

(defn sum-partition
  [partition]
  (reduce #(+ %1 (sum-size %2))
          0
          partition))

(defn process-log
   [log-file &
    {:keys [url referrer]}]
    (->> log-file
         (line-seq)
         (partition-all 5000)
         (pmap #(map parse-log %))
         (filter-by-url url)
         (filter-by-referer referrer)
         (sum-partition)))

(comment
  (time
   (let [file (io/reader "./logs/access.log.2")]
     (-> file
          (process-log {:url :all :referrer :all})
          (println))
     (.close file))))


(defn solution
  [& {:keys [url referrer]
      :or   {url :all referrer :all}}]
  (let [logs (-> "./logs"
                 get-logs-paths
                 read-logs)]
    (->> logs
         (pmap #(process-log % :url url :referrer referrer))
         (reduce +)
         (println "Bytes:"))
    (close-logs logs)))

(comment
 ;; возможные вызовы функции
 (time (solution))
 (solution :url "some-url")
 (solution :referrer "some-referrer")
 (solution :url "some-url" :referrer "some-referrer"))
