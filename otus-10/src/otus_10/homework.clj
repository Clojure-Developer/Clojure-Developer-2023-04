(ns otus-10.homework
  (:require [clojure.java.io :as io]))

(def id3-tag-identifier '(73 68 51))

(defn parse-id3-header-flags
  [byte]
  {:unsynchronisation (bit-shift-right (bit-and 0x80 byte) 7)
   :extended-header (bit-shift-right (bit-and 0x40 byte) 6)
   :experimental-indicator (bit-shift-right (bit-and 0x20 byte) 5)
   :footer-present (bit-shift-right (bit-and 0x10 byte) 4)})

(defn id3-sync-safe->int
  [bytes]
  (let [bs (vec bytes)
        b0 (get bs 0)
        b1 (get bs 1)
        b2 (get bs 2)
        b3 (get bs 3)]
    (bit-or
      (bit-shift-left b0 21)
      (bit-shift-left b1 14)
      (bit-shift-left b2 7)
      b3)))
(defn parse-id3-header
  [bytes]
  (let [bs (vec bytes)
        version (subvec bs 3 5)
        flags (get bs 5)
        size (subvec bs 6)]
    {:version (first version)
     :flags (parse-id3-header-flags flags)
     :size (id3-sync-safe->int size)}))

(defn get-id3-header [file-path]
  (with-open [input (io/input-stream file-path)]
    (let [n 10
          buf (byte-array n)
          _ (.read input buf)]
      (when (= id3-tag-identifier (take 3 buf))
        (parse-id3-header buf)))))

(defn exit
  [text code]
  (println text)
  (System/exit code))

(defn -main
  [& args]
  (when (or (< (count args) 1) (nil? (first args)))
    (exit "Pass path to mp3 file as an argument of program" -1))

  (let [file-path (first args)
        id3-header (get-id3-header file-path)]
    (when (nil? id3-header)
      (exit "Couldn't find ID3 header from mp3 file" -1))
    (println (format "Tag size is %d" (:size id3-header)))))
