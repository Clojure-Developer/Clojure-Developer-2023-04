(ns otus-06.core
  (:import [java.io File]))



;; =============================================================================
;; Работаем с окружением
;; =============================================================================

;; JAVA path

System

(import '[java.util Scanner])

(comment

 (def in-scanner
   (new Scanner System/in))

 (println "entered " (.nextInt in-scanner)))








;; Clojure path

*in*

(read-line)


(defn greet []
  (println "Enter your name:")
  (flush)
  (let [my-name (read-line)]
    (println (str "Hello " my-name))))

(greet)


;; will print only at the end the whole numbers
(doseq [x (range 20)]
  (Thread/sleep 100)
  (pr x))

;; with flush will print on every loop
(doseq [x (range 20)]
  (Thread/sleep 100)
  (pr x)
  (flush))



(with-in-str "Mr Wick"
  (greet))




*out*

(print "hello")

(.write *out* "hello\n")

(do
  (.write *out* "hello")
  (.append *out* \newline)
  (flush))



(with-out-str
  (print "some output test"))


(def out-text
  (with-out-str
    (print "some output test")))


*err*

(require '[clojure.java.shell :as shell])

(shell/sh "ls" "-la")





(System/getenv)
(System/getenv "PWD")

(System/getProperties)
(System/getProperty "os.name")



(.exists (File. "qwe"))





;; =============================================================================
;; Чтение и запись файлов
;; =============================================================================


(spit "out/sample.txt" "some test")

(slurp "out/sample.txt")

(slurp "https://www.google.com/") ;; url, reader, socket



(require '[clojure.java.io :as io])

(io/as-file "out/sample.txt")

(def sample
  (io/as-file "out/sample.txt"))

(.getName sample)
(.getAbsolutePath sample)
(.length sample)
(.isDirectory sample)
(.isFile sample)


(def rand-file
  (io/as-file "random.txt"))

(.exists rand-file)



(def sample-writer (io/writer sample))

(.write sample-writer "new text")

(.close sample-writer)



(with-open [w (io/writer sample)]
  (.append w "my new text")
  (.append w \newline))


(with-open [w (io/writer sample :append true)]
  (.append w "some other text")
  (.append w \newline))


(with-open [r (io/reader sample)]
  (vec (line-seq r)))



(defn read-nth-line [file line-number]
  (with-open [rdr (io/reader file)]
    (nth (line-seq rdr) (dec line-number))))

(read-nth-line "out/SampleCSVFile_1109kb.csv" 55)


(with-open [rdr (io/reader "https://clojuredocs.org")]
  (->> (line-seq rdr)
       (map (fn [s]
              (inc (count (.getBytes s "UTF-8")))))
       (reduce +)))




(file-seq (io/as-file "."))

(filter #(.isFile %)
        (file-seq (io/as-file ".")))


(spit "non-existing-dir/file.txt" "content")
(io/make-parents "non-existing-dir/qwe/asd/file.txt")





(io/resource "people.edn")

(->> "people.edn"
     io/resource
     slurp
     read-string
     (map :language))




(io/make-parents "result/sample.txt")

(io/copy
 (io/file "out/sample.txt")
 (io/file "result/sample.txt"))

(io/copy
 (io/reader "out/sample.txt")
 (io/writer "result/sample.txt"))



(with-open [out (io/output-stream (io/file "/tmp/zeros"))]
  (.write out (byte-array 1000)))

(with-open [in (io/input-stream (io/file "/tmp/zeros"))]
  (let [buf (byte-array 1000)
        n   (.read in buf)]
    (println "Read" n "bytes.")))

(with-open [in  (io/input-stream (io/file "/tmp/zeros"))
            out (io/output-stream (io/file "/tmp/zeros"))]
  (io/copy in out))




(str [{:foo 1}])

(prn-str {:foo 123 :bar 22})








;; =============================================================================
;; EDN extensible data notation
;; =============================================================================

(spit "out/data.edn"
      [{:user-id 1 :user-name "Ivan"}
       {:user-id 2 :user-name "Petr"}])


(require '[clojure.edn :as edn])
(import '[java.io PushbackReader])


(edn/read-string (slurp "out/data.edn"))


(try
  (with-open [r (io/reader "out/data.edn")]
   (edn/read (PushbackReader. r)))
  (catch Throwable error
    (.getMessage error)))


(def date
  #inst "2023-05-23")

(type date)


(spit "out/data.edn"
      "[#User {:id 1 :name \"Ivan\"}
        #User {:id 2 :name \"Petr\"}]")


(defrecord User [id name])

(with-open [r (io/reader "out/data.edn")]
  (let [users (edn/read
               {:readers {'User map->User}}
               (PushbackReader. r))]
    (type (first users))))






(require '[aero.core :as aero])

(aero/read-config (io/resource "config.edn"))
(aero/read-config (io/resource "config.edn") {:profile :test})
