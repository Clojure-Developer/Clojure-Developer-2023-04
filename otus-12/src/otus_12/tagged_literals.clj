(ns otus-12.tagged-literals)

(defn subdomain-kw
  "Creates a namespaced keyword from a ':subdomain/name' type keyword with ns formed by
   concatenating current ns with 'subdomain' via dot and name equal to 'name'. If the
   passed kw has no namespace the fn is equivalent to creating the keyword in the current
   ns. E.g. :foo/bar in user ns will give :user.foo/bar and just :bar will
   give :user/bar. Useful for creating specs scoped to a single fn."
  [kw]
  (if (namespace kw)
    (keyword (str (ns-name *ns*) "." (namespace kw)) (name kw))
    (keyword (str (ns-name *ns*)) (name kw))))
