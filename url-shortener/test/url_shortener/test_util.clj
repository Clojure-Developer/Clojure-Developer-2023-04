(ns url-shortener.test-util
   (:require [clojure.test :refer :all]
             [clojure.string :as str]
             [clojure.java.io :as io]))

(defn fix-test-file [file]
  (fn [t]
    (let [old-file (try
                     (slurp file)
                     (catch java.io.FileNotFoundException e nil))]
      (spit file "https://clojuredocs.org/\nhttps://www.google.com\nhttps://clojure.org\n")
      (t)
      (if (nil? old-file)
        (io/delete-file file)
        (spit file old-file)))))

(defn fix-redef-println [t]
  (with-redefs [println (fn [& args] (str (str/join " " args)))]
    (t)))
