(ns otus-15.var)

;;
;; Вары
;;

(def a 42)
a

(var a)
(deref (var a))

#'a
@#'a

(get (ns-map *ns*) 'a)
(var-get (get (ns-map *ns*) 'a))

;; Создание варов

(def ^:private everything 42)
(def ^{:private true} everything 42)

(def ^{:doc "A sample value."} b 5)
(def b
  "A sample value."
  5)

(def ^:const max-value 255)

(def ^:dynamic *max-value* 255)

(clojure.lang.Var/create 42)
;;=> #'Var: --unnamed-->
(deref (clojure.lang.Var/create 42))

;; Динамическая область видимости

(def ^:dynamic *max-value* 255)

(defn valid-value?
  [v]
  (<= v *max-value*))

(binding [*max-value* 500]
  (println (valid-value? 299))
  (future (println "in future:" (valid-value? 299)))
  (doto (Thread. #(println "in java thread:" (valid-value? 299)))
    .start
    .join))
;; => true
;; => in future: true
;; => in java thread: false

(def ^:dynamic *var* :root)

(defn get-*var*
  []
  *var*)

(with-bindings {#'*var* :a}
  (binding [*var* :b]
    (with-bindings {#'*var* :c}
      (binding [*var* :d]
        (get-*var*)))))
;; => :d

;; Неявная передача аргументов и возврат результата

(def ^:dynamic *arg*)
(def ^:dynamic *res*)

(declare ^:dynamic *arg*
         ^:dynamic *res*)

(defn implicit-function
  [value]
  (if (thread-bound? #'*arg*)
    (set! *res* (+ value *arg*))
    (set! *res* value)))

(implicit-function 1)
*res*

(binding [*res* nil
          *arg* 2]
  (implicit-function 1)
  *res*)

(defn http-get
  [url-string]
  (let [conn (.openConnection (java.net.URL. url-string))
        response-code (.getResponseCode conn)]
    (if (== 404 response-code)
      [response-code]
      [response-code (slurp (.getInputStream conn))])))

(http-get "http://google.com/bad-url")
;= [404]
(http-get "http://google.com/")
;= [200 "<!doctype html><html><head>..."]

(def ^:dynamic *response-code*)

(defn http-get
  [url-string]
  (let [conn (.openConnection (java.net.URL. url-string))
        response-code (.getResponseCode conn)]
    (when (thread-bound? #'*response-code*)
      (set! *response-code* response-code))
    (when (not= 404 response-code)
      (slurp (.getInputStream conn)))))

(http-get "http://google.com")

*response-code*

(binding [*response-code* nil]
  (let [content (http-get "http://google.com/bad-url")]
    *response-code*))

;; Изменение корневой привязки

(def ^:dynamic *some-var*)

(alter-var-root #'*some-var* (fn [_old]
                               5))

*some-var*

(with-redefs [*some-var* 4]
  *some-var*)

(with-redefs-fn {#'*some-var* 4}
  (fn []
    *some-var*))

;; binding создает области видимости поверх корневой привязки.
;; alter-var-root и with-redefs работают только с корневыми привязками.
(binding [*some-var* 3]
  (alter-var-root #'*some-var* (constantly 5))
  *some-var*)

(binding [*some-var* 3]
  (alter-var-root #'*some-var* (constantly 5))
  (with-redefs [*some-var* 4]
    *some-var*))

(with-redefs [*some-var* 4]
  (alter-var-root #'*some-var* (constantly 5))
  *some-var*)
