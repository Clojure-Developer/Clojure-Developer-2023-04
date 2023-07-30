(ns otus-15.pmap)

;;
;; Параллельная обработка по невысокой цене.
;;

(defn phone-numbers
  [s]
  (re-seq #"(\d{3})[\.-]?(\d{3})[\.-]?(\d{4})" s))

(phone-numbers " Sunil: 617.555.2937, Betty: 508.555.2218")

(def files
  (let [s (->> "Sunil: 617.555.2937, Betty: 508.555.2218"
               (concat (repeat 1000000 \space))
               (apply str))]
    (repeat 100 s)))

(time (dorun (map phone-numbers files)))
; => Elapsed time: 1623.09931 msecs
(time (dorun (pmap phone-numbers files)))
; => Elapsed time: 285.621033 msecs

(def files
  (let [s "Sunil: 617.555.2937, Betty: 508.555.2218"]
    (repeat 100000 s)))

(time (dorun (map phone-numbers files)))
; => Elapsed time: 96.869386 msecs
(time (dorun (pmap phone-numbers files)))
; => Elapsed time: 359.259886 msecs

(time (->> files
           (partition-all 250)
           (pmap (fn [chunk]
                   (doall (map phone-numbers chunk))))
           (apply concat)
           (dorun)))
; => Elapsed time: 51.00056 msecs
