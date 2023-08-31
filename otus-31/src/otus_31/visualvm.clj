(ns otus-31.visualvm)

(defn burn-cpu [op secs]
  (let [start (System/nanoTime)]
    (while (< (/ (- (System/nanoTime) start) 1e9) secs)
      (op))))

(defn test-one []
  (burn-cpu #(reduce + (map inc (range 1000))) 10))

(comment
  (test-one))
