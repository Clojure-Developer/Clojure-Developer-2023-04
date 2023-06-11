(ns otus-10.homework
  (:require [clojure.java.io :as io]))

(def tag-header-size 10)
(def frame-header-size 10)
(def tag-identifier '(73 68 51))
(def tag-identifier-size (count tag-identifier))

(defn parse-id3-header-flags
  [byte]
  {:unsynchronisation (pos? (bit-shift-right (bit-and 0x80 byte) 7))
   :extended-header (pos? (bit-shift-right (bit-and 0x40 byte) 6))
   :experimental-indicator (pos? (bit-shift-right (bit-and 0x20 byte) 5))
   :footer-present (pos? (bit-shift-right (bit-and 0x10 byte) 4))})

(defn sync-safe->int
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

(defn parse-tag-header
  "Parse tag header without tag identifier"
  [bytes]
  (let [bs (vec bytes)
        version (subvec bs 0 2)
        flags (get bs 2)
        size (subvec bs 3)]
    {:version (first version)
     :flags (parse-id3-header-flags flags)
     :size (sync-safe->int size)}))

(defn parse-frame-header
  [bytes]
  (let [bs (vec bytes)
        id (subvec bs 0 4)
        size (subvec bs 4 8)
        flags (subvec bs 8)
        int-size (sync-safe->int size)]
    {:id (apply str (map char id))
     :size int-size
     :flags flags}))

(defn read-n-bytes
  [input n]
  (let [bytes (byte-array n)
        k (max 0 (try (.read input bytes)
                      (catch Exception e (println e) 0)))]
    (if (< k n)
      [(byte-array (take k bytes)) k]
      [bytes n])))

(defn skip-ext-header
  [input]
  (let [int-size 4
        [bytes _] (read-n-bytes input int-size)
        ext-header-size (sync-safe->int bytes)
        rest-size (- ext-header-size int-size)
        [_ n] (read-n-bytes input rest-size)]
    n))

(defn skip-ext-header-if-exists
  [input tag-header]
  (if (get-in tag-header [:flags :extended-header])
    (skip-ext-header input)
    0))

(defn get-frame-header
  [input]
  (let [[bytes _] (read-n-bytes input frame-header-size)]
    (parse-frame-header bytes)))

(defmulti decode-frame-text first)
(defmethod decode-frame-text 0 [[_ & bytes]] (new String (byte-array bytes) "ISO-8859-1"))
(defmethod decode-frame-text 1 [[_ & bytes]] (new String (byte-array bytes) "UTF-16"))
(defmethod decode-frame-text 2 [[_ & bytes]] (new String (byte-array bytes) "UTF-16BE"))
(defmethod decode-frame-text 3 [[_ & bytes]] (new String (byte-array bytes) "UTF-8"))

(defmulti format-frame #(get-in % [:header :id]))
(defmethod format-frame "TALB" [{text :text}] (format "Album: %s" text))
(defmethod format-frame "TIT2" [{text :text}] (format "Name: %s" text))
(defmethod format-frame "TYER" [{text :text}] (format "Year of release: %s" text))
(defmethod format-frame "TCON" [{text :text}] (format "Genre: %s" text))
(defmethod format-frame :default [{{id :id} :header text :text}] (format "%s: %s" id text))

(defn get-frame
  [input]
  (let [header (get-frame-header input)
        [bytes _] (read-n-bytes input (:size header))
        text (decode-frame-text bytes)]
    {:header header :text text}))

(defn get-frames-seq
  [input total-frames-size]
  (when (> total-frames-size frame-header-size)
    (let [frame (get-frame input)
          frame-size (get-in frame [:header :size])
          rest-frames-size (- total-frames-size frame-size frame-header-size)]
      (lazy-seq (cons frame (get-frames-seq input rest-frames-size))))))

(defn get-frames
  [input tag-header]
  (let [total-size (:size tag-header)
        skipped (skip-ext-header-if-exists input tag-header)
        frames-size (- total-size skipped)]
    (get-frames-seq input frames-size)))

(defn read-slice-window-seq
  "Read with slice window of fixes size"
  [input n]
  (letfn [(provide-next-chunk [chunk]
            (let [[one-byte-chunk k] (read-n-bytes input 1)
                  new-chunk (if (pos? k)
                              (concat (rest chunk) one-byte-chunk)
                              nil)]
              (cons new-chunk (lazy-seq (provide-next-chunk new-chunk)))))]

    (let [[chunk k] (read-n-bytes input n)]
      (when (pos? k)
        (cons chunk
              (lazy-seq
                (provide-next-chunk chunk)))))))

(defn is-tag-identifier?
  [bytes]
  (= tag-identifier (take tag-identifier-size bytes)))

(defn get-tag-header
  [input]
  (let [chunks (read-slice-window-seq input tag-identifier-size)]
    (when (some is-tag-identifier? chunks)
      (let [rest-tag-header-size (- tag-header-size tag-identifier-size)
            [bytes _] (read-n-bytes input rest-tag-header-size)]
        (parse-tag-header bytes)))))

(defn get-tag [file-path]
  (with-open [input (io/input-stream file-path)]
    (when-let [tag-header (get-tag-header input)]
      {:header tag-header :frames (vec (get-frames input tag-header))})))

(defn exit
  [text code]
  (println text)
  (System/exit code))

(defn -main
  [& args]
  (when (or (< (count args) 1) (nil? (first args)))
    (exit "Pass path to mp3 file as an argument of program" -1))

  (let [file-path (first args)
        tag (get-tag file-path)
        {id3-header :header} tag]
    (when (nil? id3-header)
      (exit "Couldn't find ID3 header from mp3 file" -1))
    (println (format "Tag size: %d" (:size id3-header)))
    (println "Frames:")
    (doseq [frame (:frames tag)]
      (println " " (format-frame frame)))))
