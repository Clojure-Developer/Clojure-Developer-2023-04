(ns otus-10.homework
  (:require [clojure.java.io :as io]
            [otus-10.file-operations :as fo])
  (:gen-class))

(def byte->encodings
  {0 "ISO-8859-1"
   1 "UTF-16"
   2 "UTF-16BE"
   3 "UTF-8"})

(defn byte-seq->str
  [byte-seq encoding]
  (String. (byte-array byte-seq) encoding))

(defn byte-seq->int
  "Decodes ID3 big-endian integer from byte-seq into integer"
  [byte-seq]
  (reduce (fn [acc byte] (+ (* acc 128) byte)) 0 byte-seq))

(defn clear-seq-from-zeroes
  "Clears byte-seq from zeroes"
  [byte-seq]
  (filter #(not= % 0) byte-seq))

(defn decode-frame-text
  "Decodes text data from ID3v2 tag"
  [byte-seq]
  (let [encoding (get byte->encodings (first byte-seq) "ISO-8859-1")]
    (byte-seq->str (clear-seq-from-zeroes (rest byte-seq)) encoding)))

(defmulti decode-tags
  "Decodes ID3v2 tags by framename"
  (fn [frame-id-seq _] (byte-seq->str frame-id-seq "ISO-8859-1")))

(defmethod decode-tags "TALB"
  [_ byte-seq]
  {:album (decode-frame-text byte-seq)})

(defmethod decode-tags "TIT2"
  [_ byte-seq]
  {:title (decode-frame-text byte-seq)})

(defmethod decode-tags "TYER"
  [_ byte-seq]
  {:year (Integer/parseInt (decode-frame-text byte-seq) 10)})

(defmethod decode-tags "TCON"
  [_ byte-seq]
  {:genre (decode-frame-text byte-seq)})

(defmethod decode-tags :default
  [_ _]
  {})

(defn print-info
  [info]
  (let [{:keys [album title year genre]} info]
    (println "Album: " album)
    (println "Title: " title)
    (println "Year:  " year)
    (println "Genre: " genre)))

(defn parse-headers
  "Parses given byte-seq and returns headers and rest of the seq"
  [byte-seq]
  (let [headers-size 10
        [headers rest-seq] (fo/read-bytes byte-seq headers-size)
        flags (nth headers 5)
        extended-header-exist? (not= (bit-and flags 0x40) 0)
        tags-size-bytes (->> headers (drop 6) (take 4))
        tags-size (byte-seq->int tags-size-bytes)]
    [{:extended-header-exist? extended-header-exist? :tags-size tags-size} rest-seq]))

(defn skip-extended-header
  [[headers byte-seq]]
  (if (:extended-header-exist? headers)
    (let [extended-header-size-bytes (take 4 byte-seq)
          extended-header-size (byte-seq->int extended-header-size-bytes)]
      [headers (drop extended-header-size byte-seq)])
    [headers byte-seq]))

(defn parse-frame-headers
  [byte-seq]
  (let  [[frame-id rest-seq] (fo/read-bytes byte-seq 4)
         [frame-size-bytes rest-seq] (fo/read-bytes rest-seq 4)
         frame-size (byte-seq->int frame-size-bytes)]
    [{:frame-id frame-id :frame-size frame-size} (rest rest-seq)]))

(defn parse-frame-data
  [[frame-headers byte-seq]]
  (let [frame-data-size (inc (:frame-size frame-headers)) ;; +1 for encoding byte
        [frame-data rest-seq] (fo/read-bytes byte-seq frame-data-size)]
    [(decode-tags (:frame-id frame-headers) frame-data) rest-seq]))

(defn parse-frames
  [[headers byte-seq]]
  (let [tags-size (:tags-size headers)
        [frames] (fo/read-bytes byte-seq tags-size)]
    (loop [frames frames
           info {}]
      (if (empty? frames)
        info
        (let [[frame-data
               rest-seq] (-> frames
                             parse-frame-headers
                             parse-frame-data)]
          (recur rest-seq (merge info frame-data)))))))

(defn read-mp3-file
  "Reads an MP3 file from `src`"
  [src]
  (let [istream (io/input-stream src)]
    (fo/lazy-byte-reader istream)))

(defn build-info
  [src]
  (-> src
      read-mp3-file
      parse-headers
      skip-extended-header
      parse-frames))

(defn run-app
  "Runs the app"
  [src]
  (let [info (build-info src)]
    (print-info info)))

(defn -main
  "CLI util to show info from ID3v2 header of mp3 file.

  The main task:
  1. Find ID3v2 header
  2. Prctice multimethod on decoding text data (ID3v2 has 4 different encodings)
  3. Three multimethods for decoding tags: TALB - album, TIT2 - title, TYER - year, TCON - genre"
  [src & args]
  (run-app src))
