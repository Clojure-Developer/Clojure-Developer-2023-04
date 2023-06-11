(ns ^{:doc "File operations"}
    otus-10.file-operations
  (:import [java.io InputStream]))

(defn lazy-byte-reader
    "Opens the file and creates a lazy-sequence of the bytes"
    [^InputStream istream]
    (lazy-seq
      (let [byte (.read istream)]
        (if (>= byte 0)
          (cons (short (bit-and byte 0xFF)) (lazy-byte-reader istream))
          (do (.close istream) nil)))))

(defn read-bytes
  "Reads given number of bytes.
  Returns `nil` if unable to read given number of bytes"
  [byte-seq size]
  (let [bytes (take size byte-seq)
        bytes-count (count bytes)]
    (if (= bytes-count size)
        [bytes (drop size byte-seq)]
        [nil])))
