(ns otus-13.core
  (:require
   [clj-http.client :as http]
   [clojure.java.io :as io]
   [cheshire.core :as cheshire]
   [clojure.data.csv :as csv])
  (:import
   [java.io InputStream]))


(slurp "http://localhost:80/json")





;; GET, POST, PUT, DELETE

(http/get "http://localhost:80/json")


(http/get "http://localhost:80/json"
          {:as :json})


(http/get "http://localhost:80/headers"
          {:headers {:x-otus-course "Clojure Developer"}
           :as      :json})


(http/get "http://localhost:80/headers"
          {:headers {:x-otus-course "Clojure Developer"}
           :accept  "text/html"
           :as      :json})


;; All request options

;; :url
;; :method
;; :query-params
;; :basic-auth
;; :content-type
;; :accept
;; :accept-encoding
;; :as
;; :headers
;; :body
;; :connection-timeout
;; :connection-request-timeout
;; :connection-manager
;; :cookie-store
;; :cookie-policy
;; :multipart
;; :query-string
;; :redirect-strategy
;; :max-redirects
;; :retry-handler
;; :request-method
;; :scheme
;; :server-name
;; :server-port
;; :socket-timeout
;; :uri
;; :response-interceptor
;; :proxy-host
;; :proxy-port
;; :http-client-context
;; :http-request-config
;; :http-client
;; :proxy-ignore-hosts
;; :proxy-user
;; :proxy-pass
;; :digest-auth
;; :ntlm-auth
;; :multipart-mode
;; :multipart-charset


(http/post "http://localhost:80/post"
           {:body         "{\"foo\": \"bar\"}"
            :content-type :json
            :as           :json})


;; as a urlencoded body
(http/post "http://localhost:80/post"
           {:form-params {:foo "bar"}
            :as          :json})


(http/post "http://localhost:80/post"
           {:form-params  {:foo "bar"}
            :content-type :json
            :as           :json})


(http/put "http://localhost:80/put"
          {:as :json})


(http/delete "http://localhost:80/delete"
             {:as :json})


(http/request {:url    "http://localhost:80/delete"
               :method :delete
               :as     :json})





;; coercion

;; :byte-array, :json, :json-string-keys, :transit+json, :transit+msgpack, :clojure,
;; :x-www-form-urlencoded, :stream, :reader

(def response
  (http/get "http://localhost:80/stream/10"
            {:as :stream}))


(type (:body response))

(isa? (class (:body response)) InputStream)


(->> (io/reader (:body response))
     (line-seq)
     (mapv #(cheshire/parse-string % true)))



(let [response (http/get "http://localhost:80/stream/10" {:as :reader})]
  (with-open [reader (:body response)]
    (doall
     (for [line (line-seq reader)]
       (cheshire/parse-string line true)))))





;; custom CSV coercion
(defmethod http/coerce-response-body :csv [request {:keys [body] :as response}]
  (if (or (http/server-error? response)
          (http/client-error? response))
    response

    (-> (slurp body)
        (cheshire/parse-string true)
        :data
        (csv/read-csv)
        (as-> $
          (assoc response :body $)))))


(def my-csv-str
  "name,age,salary
Mbappe,24,72M
Messi,35,35M
Ronaldo,38,72M")


(-> (http/post "http://localhost:80/anything"
               {:body my-csv-str
                :as   :csv})
    :body)





;; exceptions, slingshot

(http/get "http://localhost:80/status/400")


(http/get "http://localhost:80/status/500")


(http/get "http://localhost:80/status/500"
          {:throw-entire-message? true})


(http/get "http://localhost:80/status/500"
          {:throw-exceptions false})


(http/server-error?
 (http/get "http://localhost:80/status/500"
           {:throw-exceptions false}))


(http/get "http://localhost:80/status/250"
          {:unexceptional-status #(<= 200 % 249)})





(require '[slingshot.slingshot :refer [throw+ try+]])

(try+
 (throw+ {:foo "bar"})
 (throw+ 152)
 (throw+ (ex-info "Error" {:foo "baz"}))
 (throw+ (Exception. "Exception"))
 (throw+ (Throwable. "Error"))

 (catch [:foo "bar"] thrown-map
   (println "caught a map")
   (println thrown-map))

 (catch integer? i
   (println "caught a number")
   (println i))

 (catch Exception ex
   (println "caught exception")
   (println ex))

 (catch Throwable t
   (println "caught throwable")
   (println t)))



(try+
 (http/get "http://localhost:80/status/403")
 (http/get "http://localhost:80/status/404")
 (http/get "http://localhost:80/status/500")

 (catch [:status 403] {:keys [request-time headers body]}
   (println "403" request-time headers))

 (catch [:status 404] {:keys [request-time headers body]}
   (println "NOT Found 404" request-time headers body))

 (catch Object _
   (println "unexpected error" (:message &throw-context))))





;; async request
(http/get "http://localhost:80/delay/5"
          {:async? true :as :json}
          (fn [response] (println "response is:" (:body response)))
          (fn [exception] (println "exception message is: " (.getMessage exception))))





;; pagination
(defn get-data-page [page]
  (println "page request" page)
  (-> (http/post "http://localhost:80/anything"
                 {:as           :json
                  :content-type :json
                  :form-params  {:data (->> (range)
                                            (drop (* page 5))
                                            (take 5))
                                 :next (when (< page 10)
                                         (inc page))}})
      :body :data
      (cheshire/parse-string true)))


(defn get-paginated-data [current-page]
  (lazy-seq
   (let [page      (get-data-page current-page)
         page-data (:data page)
         next-page (:next page)]
     (cons page-data
           (when (some? next-page)
             (get-paginated-data next-page))))))


(def data
  (flatten (get-paginated-data 0)))


(type data)

(take 30 data)





;; iteration
(def data-2
  (->> (iteration get-data-page
                  :initk 0
                  :kf :next
                  :vf :data)
       (sequence cat)))

(take 40 data-2)




;; lazy concat
(defn lazy-concat [colls]
  (lazy-seq
   (when-first [c colls]
     (lazy-cat c (lazy-concat (rest colls))))))


(def data-3
  (->> (iteration get-data-page
                  :initk 0
                  :kf :next
                  :vf :data)
       (lazy-concat)))

(take 20 data-3)


(->> (get-paginated-data 0)
     (lazy-concat)
     (take 10))
