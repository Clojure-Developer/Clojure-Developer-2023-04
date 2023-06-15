(ns id3-tag.frame)

(def start-map {:data []
                :parse-step :header
                :size 10})
(def encoding-table {
                     0x00 "ISO-8859-1"
                     0x01 "UTF-16"
                     0x02 "UTF-16BE"
                     0x03 "UTF-8"})

(defn full-size? [acc]
  (= (count (:data acc))
     (:size acc)))

(defn calc-length [v]
  (reduce #(+ (* 128 %1) %2) v))

(defn with-ext-header? [flag]
  (pos? (bit-and flag 2r01000000)))

(defn ->String [coll charset]
  (-> coll
      vec
      byte-array
      (String. charset)))
  

(defmulti parse-step :parse-step)

(defmethod parse-step :header
  [acc]
  (let [flag (nth (:data acc) 5)]
    (if (with-ext-header? flag)
      (-> acc
          (assoc :size 10)
          (assoc :data [])
          (assoc :parse-step :extra-header))
      (-> acc
          (assoc :size 10)
          (assoc :data [])
          (assoc :parse-step :frame-header)))))

(defmethod parse-step :extra-header
  [acc]
  (let [size (calc-length (take 4 (:data acc)))]
    (-> acc
        (assoc :size size)
        (assoc :parse-step :skip-ex-header))))

(defmethod parse-step :skip-ex-header
  [acc]
  (-> acc
      (assoc :size 10)
      (assoc :parse-step :frame-header)
      (assoc :data [])))
  
(defmethod parse-step :frame-header
  [acc]
  (let [name (->> (:data acc)
                  (take 4)
                  (#(->String % "ISO-8859-1")))
        size (->> (:data acc)
                  (drop 4)
                  (take 4)
                  calc-length)]
    (when (pos? size)
      ;; Я изначально думал опираться на длину из заголовка тега
      ;; Но выяснилось, что она не всегда соответсвует реальности
      ;; Нашел такой метод признака останова
      (-> acc
          (assoc :size size)
          (assoc :data [])
          (assoc :frame name)
          (assoc :parse-step :frame)))))

(defmethod parse-step :frame
  [acc]
  (let [[flag & body] (:data acc)
        sbody (->String body (encoding-table flag))]
    (-> acc
        (assoc :tag [(:frame acc) sbody])
        (assoc :size 10)
        (assoc :data [])
        (assoc :parse-step :frame-header)
        (dissoc :frame))))
    

(defmethod parse-step :default
  [acc]
  acc)


(defn frame 
  [rf]
  (fn
    ([] (rf))
    
    ([acc b]
     (let [new-acc (update acc :data conj b)]
       (if (full-size? new-acc)
         (if-let [parse-acc (parse-step new-acc)]
           (rf parse-acc b)
           (reduced new-acc))
         new-acc)))
    
    ([acc]
     (reduce #(dissoc %1 %2) acc
             [:data :size :parse-step :frame :tag]))))
     
