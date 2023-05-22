(ns example.core)

(defn greet
  "Greets someone"
  ([] (greet "Noname"))
  ([name] (println (str "Hello, " name "!"))))
