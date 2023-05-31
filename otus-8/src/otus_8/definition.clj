(ns otus-8.definition)

(defn- func
  []
  nil)

(defn ^:private func
  []
  nil)

(def ^:private variable
  nil)

(meta #'func)
(meta #'variable)

(defn get-file
  ^String [^Long x]
  x)

(set! *print-meta* true)

(defn get-file
  "Some docstring"
  {:error [:exception/null-pointer]
   :pre ()
   :post ()}
  ^String [^Long x]
  x)
