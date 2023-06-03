(ns otus-08.reflection)

(set! *warn-on-reflection* true)

(defn capitalize
  "string => String"
  [s]
  (-> (.charAt s 0)
      (Character/toUpperCase)
      (str (.substring s 1))))

(defn capitalize-fast
  "string => String"
  [^String s]
  (-> (.charAt s 0)
      (Character/toUpperCase)
      (str (.substring s 1))))

(time
 (doseq [s (repeat 100000 "foo")]
   (capitalize s)))

(time
 (doseq [s (repeat 100000 "foo")]
   (capitalize-fast s)))

(defn file-extension
  ^String [^java.io.File f]
  (-> (re-seq #"\.(.+)" (.getName f))
      (first)
      (second)))

(.toUpperCase (file-extension (java.io.File. "image.png")))
