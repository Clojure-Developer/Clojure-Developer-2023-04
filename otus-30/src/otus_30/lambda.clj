(ns otus-30.lambda
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))


(defn -handleRequest
  "Implementation for RequestStreamHandler that handles a Lambda Function request"
  [_ input-stream output-stream context]
  (spit output-stream "Hello, World!"))
