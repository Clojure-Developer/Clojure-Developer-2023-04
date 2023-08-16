(ns otus-28.streams
  (:require [clojure.string :as str]
            [jackdaw.streams :as js]
            [jackdaw.client :as jc]
            [jackdaw.client.log :as jcl]
            [jackdaw.admin :as ja]
            [jackdaw.serdes.edn :refer [serde]])
  (:import [java.util UUID]))


;; The config for our Kafka Streams app
(def kafka-config
  {"application.id"            "kafka-streams-the-clojure-way"
   "bootstrap.servers"         "localhost:9092"
   "default.key.serde"         "jackdaw.serdes.EdnSerde"
   "default.value.serde"       "jackdaw.serdes.EdnSerde"
   "cache.max.bytes.buffering" "0"})


;; Serdes tell Kafka how to serialize/deserialize messages
(def serdes
  {:key-serde   (serde)
   :value-serde (serde)})


;; =============================================================================
;; Приложение для обработки покупок
;; =============================================================================

;; Each topic needs a config. The important part to note is the :topic-name key.
(def purchase-made-topic
  (merge {:topic-name         "purchase-made"
          :partition-count    1
          :replication-factor 1}
         serdes))


(def large-transaction-made-topic
  (merge {:topic-name         "large-transaction-made"
          :partition-count    1
          :replication-factor 1}
         serdes))


;; An admin client is needed to do things like create and delete topics
(def admin-client (ja/->AdminClient kafka-config))



(defn make-purchase! [amount]
  "Publish a message to the purchase-made topic, with the specified amount"
  (let [purchase-id (rand-int 10000)
        user-id     (rand-int 10000)
        quantity    (inc (rand-int 10))]
    (with-open [producer (jc/producer kafka-config serdes)]
      @(jc/produce! producer purchase-made-topic purchase-id {:id       purchase-id
                                                              :amount   amount
                                                              :user-id  user-id
                                                              :quantity quantity}))))

(defn view-messages [topic]
  "View the messages on the given topic"
  (with-open [consumer (jc/subscribed-consumer
                        (assoc kafka-config "group.id" (str (UUID/randomUUID)))
                        [topic])]
    (jc/seek-to-beginning-eager consumer)
    (->> (jcl/log-until-inactivity consumer 100)
         (map :value)
         doall)))


(defn simple-topology [builder]
  ;; создаём новый поток
  (-> (js/kstream builder purchase-made-topic)
      ;; оставляем только те заказы для которых :amount больше 100
      (js/filter (fn [[_ purchase]]
                   (<= 100 (:amount purchase))))
      ;; трансформируем сообщения
      (js/map (fn [[key purchase]]
                [key (select-keys purchase [:amount :user-id])]))
      ;; перенаправляем сообщения в другой топик
      (js/to large-transaction-made-topic)))


(defn start! []
  "Starts the simple topology"
  (let [builder (js/streams-builder)]
    (simple-topology builder)
    (doto (js/kafka-streams builder kafka-config)
      (js/start))))


(defn stop! [kafka-streams-app]
  "Stops the given KafkaStreams application"
  (js/close kafka-streams-app))



(comment

 (ja/create-topics!
  admin-client
  [purchase-made-topic
   large-transaction-made-topic])

 (def app
   (start!))

 (make-purchase! 50)
 (make-purchase! 20)
 (make-purchase! 200)
 (make-purchase! 150)

 (view-messages purchase-made-topic)
 (view-messages large-transaction-made-topic)

 (stop! app))




;; =============================================================================
;; Приложение для подсчёта слов
;; =============================================================================

(def text-topic
  (merge {:topic-name         "text-input"
          :partition-count    1
          :replication-factor 1}
         serdes))


(def counts-topic
  (merge {:topic-name         "word-counts"
          :partition-count    1
          :replication-factor 1}
         serdes))


(defn split-lines
  "Takes an input string and returns a list of words with the whitespace removed."
  [s]
  (str/split (str/lower-case s) #"\W+"))


(defn word-count-topology
  "Takes topic metadata and returns a function that builds the topology."
  [builder]
  ;; создаём новый поток из сообщений топика text-topic
  (let [text-input (js/kstream builder text-topic)

        counts     (-> text-input
                       ;; разделяем текст на слова
                       ;; каждое слово становится отдельным сообщением в потоке
                       (js/flat-map-values split-lines)
                       ;; группируем сообщения по слову
                       ;; агрегирующие операторы возвращают KTable
                       (js/group-by (fn [[_ word]] word))
                       (js/count))]

    (-> counts
        ;; переводим KTable обратно в KStream
        (js/to-kstream)
        ;; записываем сообщения в другой топик
        (js/to counts-topic))))


(def streams-config
  {"application.id"            "word-count"
   "bootstrap.servers"         "localhost:9092"
   "default.key.serde"         "jackdaw.serdes.EdnSerde"
   "default.value.serde"       "jackdaw.serdes.EdnSerde"
   "cache.max.bytes.buffering" "0"})


(defn wc-app []
  (let [builder (js/streams-builder)]
    (word-count-topology builder)
    (doto (js/kafka-streams builder streams-config)
      (js/start))))




(comment

 (ja/create-topics! admin-client [text-topic counts-topic])

 (def app
   (wc-app))


 (with-open [producer (jc/producer streams-config serdes)]
   @(jc/produce! producer text-topic nil "all streams lead to kafka"))

 (with-open [producer (jc/producer streams-config serdes)]
   @(jc/produce! producer text-topic nil "hello kafka streams"))


 (view-messages text-topic)
 (view-messages counts-topic)


 (with-open [consumer (jc/subscribed-consumer
                       (assoc streams-config "group.id" (str (UUID/randomUUID)))
                       [counts-topic])]
   (jc/seek-to-beginning-eager consumer)
   (->> (jcl/log consumer 100 seq)
        (doall)
        (map (juxt :key :value))))

 nil)
