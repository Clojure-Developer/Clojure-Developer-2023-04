(ns otus-8.util)

(defn cel->fahr
  [cel]
  (if (number? cel)
    (+ (* cel 1.8) 32)
    (throw (IllegalArgumentException. "Fahrenheit temperature must be a real number"))))
