(ns id3-tag.file)


(defn id3? [in]
  (= "ID3"
     (String. (.readNBytes in 3) "ISO-8859-1")))


(defn file->seq [in]
  (for [_ (range 300)]
    (.read in)))
   



