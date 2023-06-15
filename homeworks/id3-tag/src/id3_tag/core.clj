(ns id3-tag.core
  (:require [id3-tag.file :as file]
            [clojure.java.io :as io]
            [id3-tag.frame :as frame]
            [id3-tag.tag :as tag]))


(defn parse-id3 [name]
  (with-open [in (io/input-stream name)]
    (transduce (comp frame/frame tag/tag) merge frame/start-map (file/file->seq in))))

(defn -main [name & args]
  (println (parse-id3 name)))
  

;; (-main "file1.mp3")



