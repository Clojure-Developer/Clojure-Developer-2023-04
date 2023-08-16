(ns otus-28.java-client
  (:import
   [org.apache.kafka.clients.admin AdminClientConfig NewTopic KafkaAdminClient]
   [org.apache.kafka.clients.consumer ConsumerRecord KafkaConsumer]
   [org.apache.kafka.clients.producer KafkaProducer ProducerRecord]
   [org.apache.kafka.common.serialization StringDeserializer StringSerializer]
   [java.time Duration]))


(def bootstrap-server "localhost:9092")

(def topic "example-consumer-topic")



(defn create-topic!
  [bootstrap-server topic]
  (let [config      {AdminClientConfig/BOOTSTRAP_SERVERS_CONFIG bootstrap-server}
        adminClient (KafkaAdminClient/create config)
        {:keys [^String topic-name ^Integer partitions ^Short replication]} topic
        kafka-topic (NewTopic. topic-name partitions replication)]
    ;; создаем новый топик (можно создать сразу несколько)
    (.createTopics adminClient [kafka-topic])))



(defn make-consumer ^KafkaConsumer [bootstrap-server]
  (let [consumer-props
        {"bootstrap.servers",  bootstrap-server
         "group.id",           "example"
         "key.deserializer",   StringDeserializer
         "value.deserializer", StringDeserializer
         "auto.offset.reset",  "earliest"
         "enable.auto.commit", "true"}]
    (KafkaConsumer. consumer-props)))



(defn make-producer [bootstrap-server]
  (let [producer-props
        {"value.serializer"  StringSerializer
         "key.serializer"    StringSerializer
         "bootstrap.servers" bootstrap-server}]
    (KafkaProducer. producer-props)))



(defn start-consumer [bootstrap-server]
  (let [consumer ^KafkaConsumer (make-consumer bootstrap-server)]

    ;; подписываем консьюмер на топик
    (.subscribe consumer [topic])

    (let [close? (promise)]
      ;; start in a new thread
      (future
       (while (not (realized? close?))
         ;; запрашиваем новые сообщения
         (let [records (.poll consumer (Duration/ofMillis 100))]
           (doseq [^ConsumerRecord record records]
             (println "message received:" (.value record))))
         ;; уведомляем брокер что обработали все сообщения
         (.commitAsync consumer))
       ;; закрываем консьюмер
       (.close consumer))

      ;; callback для остановки консьюмера
      #(deliver close? true))))


(comment


 ;; приложение для обработки сообщений
 (create-topic!
  bootstrap-server
  {:topic-name topic :partitions 1 :replication 1})

 (def stop-consumer
   (start-consumer bootstrap-server))




 ;; приложение для отправки сообщений
 (def counter
   (atom 0))

 (def producer
   (make-producer bootstrap-server))

 (defn record [producer]
   (ProducerRecord.
    topic
    "message-key"
    (str "current counter value - " (swap! counter inc)
         " from producer - " producer)))

 ;; отправляем сообщение в брокер
 (.send producer (record "test"))




 (doseq [n (range 4)]
   (future
    (with-open [p (make-producer bootstrap-server)]
      (dotimes [t 20]
        (Thread/sleep ^Long (rand-int 500))
        (.send p (record n))))))



 ;; останавливаем приложение
 (stop-consumer)

 nil)
