(ns kafka-web-scraper.core
  (:require
   [clj-http.client :as http]
   [jackdaw.streams :as js]
   [jackdaw.client :as jc]
   [jackdaw.client.log :as jcl]
   [jackdaw.admin :as ja]
   [jackdaw.serdes.edn :refer [serde]])
  (:import
   [org.jsoup Jsoup]
   [org.jsoup.nodes Element]))





(def jobs-url
  "https://remoteok.com")


(defn get-job-links
  "Скачиваем главную страницу и достаём ссылки на вакансии"
  []
  (let [html  ^String (-> jobs-url http/get :body)
        page  (Jsoup/parse html)
        links (-> page
                  (.select "#jobsboard")
                  (.select "tr.job"))]
    (map (fn [el]
           {:id   (.attr el "id")
            :link (str jobs-url (.attr el "data-href"))})
         links)))


(defn get-job
  "Скачиваем страницу с одной вакансией"
  [link]
  (let [html (-> link http/get :body)]
    html))


(defn get-info
  "Парсим страницу с вакансией и достаём краткую информацию"
  [^String html]
  (let [page     (Jsoup/parse html)
        info-el  (.select page "td.company_and_position")
        company  (-> info-el
                     (.select "span.companyLink")
                     (.select "h3[itemprop=name]"))
        position (-> info-el
                     (.select "a[itemprop=url]")
                     (.select "h2"))
        salary   (-> (.select info-el "> *")
                     last)]

    {:company  (.text company)
     :position (.text position)
     :salary   (.text ^Element salary)}))



(comment
 (-> (get-job-links)
     first)

 (-> (get-job-links)
     first
     :link
     (get-job)
     (get-info)))





(def kafka-config
  {"application.id"            "jobs-scraper"
   "bootstrap.servers"         "localhost:9092"
   "default.key.serde"         "jackdaw.serdes.EdnSerde"
   "default.value.serde"       "jackdaw.serdes.EdnSerde"
   "cache.max.bytes.buffering" "0"})

(def serdes
  {:key-serde   (serde)
   :value-serde (serde)})


;; Создаём топики для Kafka приложения
(def jobs-links-topic
  (merge {:topic-name         "jobs-links-topic"
          :partition-count    1
          :replication-factor 1}
         serdes))

(def jobs-summary-topic
  (merge {:topic-name         "jobs-summary-topic"
          :partition-count    1
          :replication-factor 1}
         serdes))

(def jobs-description-topic
  (merge {:topic-name         "jobs-description-topic"
          :partition-count    1
          :replication-factor 1}
         serdes))


(defn fetch-jobs-topology [builder]
  ;; создаём поток который будет содержать ссылки на вакансии
  (let [jobs (-> (js/kstream builder jobs-links-topic)
                 ;; каждую ссылку преобразуем в HTML страницы с вакансией
                 (js/map (fn [[key job-link]]
                           [key (get-job job-link)])))]

    ;; создаём отдельный поток для краткой выжимки по вакансии
    (-> jobs
        (js/map (fn [[key job]]
                  [key (get-info job)]))
        (js/to jobs-summary-topic))

    ;; полное содержимое страницы отправляем в другой топик
    (-> jobs
        (js/to jobs-description-topic))))


(defn start! []
  (let [builder (js/streams-builder)]
    (fetch-jobs-topology builder)
    (doto (js/kafka-streams builder kafka-config)
      (js/start))))


(defn stop! [kafka-streams-app]
  (js/close kafka-streams-app))




(comment

 (def admin-client
   (ja/->AdminClient kafka-config))

 (ja/create-topics!
  admin-client
  [jobs-links-topic
   jobs-summary-topic
   jobs-description-topic])

 (def app
   (start!))



 ;; запускаем продьюсер сообщений
 (defn fetch-jobs-links []
   (with-open [producer (jc/producer kafka-config serdes)]
     (doseq [{:keys [id link]} (get-job-links)]
       (jc/produce! producer jobs-links-topic id link))))

 (fetch-jobs-links)



 (defn view-messages [topic]
   "View the messages on the given topic"
   (with-open [consumer (jc/subscribed-consumer
                         (assoc kafka-config "group.id" (str (java.util.UUID/randomUUID)))
                         [topic])]
     (jc/seek-to-beginning-eager consumer)
     (->> (jcl/log-until-inactivity consumer 100)
          (map (juxt :key :value))
          doall)))

 (view-messages jobs-links-topic)                           ;; all links
 (view-messages jobs-summary-topic)                         ;; send to analytics dashboard
 (view-messages jobs-description-topic)                     ;; save to S3

 (stop! app))

