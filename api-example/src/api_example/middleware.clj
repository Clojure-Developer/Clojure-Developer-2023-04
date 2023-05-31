(ns api-example.middleware)

(defn assoc-to-request
  [handler key value]
  (fn [request]
    (handler (assoc request key value))))

(defn wrap-request-ctx
  [handler ctx]
  (assoc-to-request handler :ctx ctx))

(defn wrap-postgres-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch org.postgresql.util.PSQLException e
        {:status 400
         :body {:message (ex-message e)
                :data (ex-data e)}}))))

(defn wrap-fallback-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        {:status 500
         :body {:message (ex-message e)
                :data (ex-data e)}}))))
