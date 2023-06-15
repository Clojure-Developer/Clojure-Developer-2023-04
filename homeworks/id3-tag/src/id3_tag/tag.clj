(ns id3-tag.tag)

(defmulti parse-tag first)

(defmethod parse-tag :default
  [[_ _]]
  nil)

(defmethod parse-tag "TALB"
  [[_ v]]
  {:album v})
 
(defmethod parse-tag "TIT2"
  [[_ v]]
  {:title v})

(defmethod parse-tag "TCON"
  [[_ v]]
  {:ganre v})

(defmethod parse-tag "TYER"
  [[_ v]]
  (let [year (try (Integer/parseInt v)
                  (catch Exception _ nil))]
    {:year year}))


(defn tag 
  [rf]
  (fn
    ([] 
     (rf))
    
    ([acc _]
     (if (:tag acc)
       (-> (:tag acc)
           parse-tag
           (#(apply (partial rf acc) %))
           (dissoc :tag))
       acc))
           
    ([acc] acc)))
