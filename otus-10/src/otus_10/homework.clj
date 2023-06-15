(ns otus-10.homework
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(def encoding-table {
                     0x00 "ISO-8859-1"
                     0x01 "UTF-16"
                     0x02 "UTF-16BE"
                     0x03 "UTF-8"})

(defn calc-length [in]
  (->> (.readNBytes in 4)
       vec
       (reduce #(+ (* 128 %1) %2))))
  
(defn with-ext-header? [flag]
  (pos? (bit-and flag 2r01000000)))

(defn skip-header [in]
  (.skip in 5)
  (let [flag (.read in)
        size (calc-length in)]
    (println size)
    (when (with-ext-header? flag)
      (let [size-ex (calc-length in)]
        (println "flag" flag)
        (.skip in (- size-ex 4))))
    size))


(defn load-frame [in]
  (let [frame-id (String. (.readNBytes in 4) "ISO-8859-1")
        size (calc-length in)
        flag (.read in)]
    (println size)
    (read-line)
    (.skip in 2)
    (if (pos? size) ;; Не нашел другого способа ловить конец тега ID3v2
      [(+ 10 size) [frame-id (-> (.readNBytes in (dec size))
                                 (String. (encoding-table flag))
                                 str/trim)]]
                                 
      nil)))
    
(with-open [in (io/input-stream (io/file "file3.mp3"))]
 (let [size (skip-header in)]
   (loop [tag-size size]
     (let [[f-size [frame-id frame-text]] (load-frame in)]
       (when-not (nil? f-size)
         (println frame-id frame-text tag-size f-size)
         (recur (- tag-size f-size)))))))

  
  
  
  

   
   
   
  ;; let [flags by
  ;;       buf1 (byte-array 10)
  ;;       n   (.read in buf)
  ;;       m (.read in buf1)]
  ;;   (println "Read" n m "bytes.")
  ;;   (println (String. buf "UTF-16BE"))
  ;;   (prn buf1)
    
