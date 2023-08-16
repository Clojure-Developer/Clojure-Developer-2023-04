(ns otus-28.jackdaw-client
  (:require
   [jackdaw.admin :as ja]
   [jackdaw.client :as jc])
  (:import [org.apache.kafka.common.serialization Serdes]))


(def bootstrap-server "localhost:9092")


(def client
  (ja/->AdminClient {"bootstrap.servers" bootstrap-server}))


(defn create-topic []
  (ja/create-topics!
   client
   [{:topic-name         "jackdaw-example"
     :partition-count    10
     :replication-factor 1
     :topic-config       {"cleanup.policy" "compact"}}]))




(def consumer-config
  {"bootstrap.servers" bootstrap-server
   "group.id"          "my-consumer-group"})


(defn start-consumer []
  (let [consumer (jc/subscribed-consumer
                  consumer-config
                  [{:topic-name  "jackdaw-example"
                    :key-serde   (Serdes/String)
                    :value-serde (Serdes/String)}])
        close?   (promise)]

    (future
     (while (not (realized? close?))
       (let [records (jc/poll consumer 100)]
         (when (seq records)
           (doseq [record records]
             (println "message received:" (:value record)))
           (.commitSync consumer))))
     (.close consumer))

    #(deliver close? true)))




(comment


 (create-topic)

 (jackdaw.admin/list-topics client)



 (def stop-consumer
   (start-consumer))



 (def my-producer
   (jc/producer {"bootstrap.servers" bootstrap-server}
                {:key-serde   (Serdes/String)
                 :value-serde (Serdes/String)}))

 (jc/produce!
  my-producer {:topic-name "jackdaw-example"}
  "message-key"
  "Hello from Jackdaw")


 nil)
