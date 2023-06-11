(ns otus-10.core
  (:import [java.lang.annotation Retention RetentionPolicy])
  (:gen-class))


(definterface IAppend
  (append [x]))

;; reify: a fancy philosophical word that means
;; "make a concrete thing out of an abstract concept"

(defn grocery-list
  "Creates an appendable grocery list. Takes a vector of groceries to buy."
  [to-buy]
  (reify IAppend
    (append [this x]
      (grocery-list (conj to-buy x)))))

(grocery-list [:eggs])

(supers (type (grocery-list [:eggs])))
(meta (grocery-list [:eggs]))

(.append (grocery-list [:eggs]) :tofu)

;;(map .append [(grocery-list [:eggs])] [:tofu])

#_:clj-kondo/ignore
(defn grocery-list
  "Creates an appendable grocery list. Takes a vector of groceries to buy."
  [to-buy]
  (reify
    IAppend
    (append [this x]
      (grocery-list (conj to-buy x)))

    Object
    (toString [this]
      (str "To buy: " to-buy))))

(str (.append (grocery-list [:eggs]) :tomatoes))

#_(.append [1 2] 3)

;; proxy
(let [p (proxy [java.io.InputStream] []
          (read
            ([] 1)
            ([^bytes bytes] 2)
            ([^bytes bytes off len] 3))
          (toString
            ([] (str (.hashCode this)))))]
  (println (.read p))
  (println (.read p (byte-array 3)))
  (println (.read p (byte-array 3) 0 3))
  (println p))

;; "Expression problem"
;; Existing (regular) functions can’t be extended to new types, and existing
;; types can’t be extended to new interfaces.

;; Protocols 

;; Protocol is like an interface which can be extended to existing types.
;; It defines a named type, with functions whose 1st argument is
;;  an instance of that type.

(defprotocol Append
  "This protocol lets us add things to the end of a collection."
  (append [coll x]
    "Appends x to the end of collection coll."))

;; (doc Append)
;; (doc append)

#_(append (grocery-list [:eggs]) :tomatoes)

#_:clj-kondo/ignore
(defn grocery-list
  "Creates an appendable grocery list. Takes a vector of groceries to buy."
  [to-buy]
  (reify
    Append
    (append [this x]
      (grocery-list (conj to-buy x)))

    Object
    (toString [this]
      (str "To buy: " to-buy))))

(str (append (grocery-list [:eggs]) :tomatoes))

#_(append [1 2] 3)

(extend-protocol Append
  clojure.lang.IPersistentVector
  (append [v x]
    (conj v x)))

#_(append '() 2)

(extend-protocol Append
  clojure.lang.IPersistentVector
  (append [v x]
    (conj v x))

  clojure.lang.Sequential
  (append [v x]
    (concat v (list x))))

#_(append nil 2)

(extend-protocol Append
  nil
  (append [_ x]
    [x]))

;; deftype 

;; reify creates anonymous type

(deftype GroceryList [to-buy]
  Append
  (append [this x]
    (GroceryList. (conj to-buy x)))

  Object
  (toString [this]
    (str "To buy: " to-buy)))

(GroceryList. [:eggs])

(append (GroceryList. [:eggs]) :spinach)

(supers GroceryList)

(.to-buy (GroceryList. [:eggs]))

(->GroceryList [:strawberries])

;; (map GroceryList. [[:twix] [:kale :bananas]])
(map ->GroceryList [[:twix] [:kale :bananas]])

;; deftype is bare-bones
(= (GroceryList. [:cheese]) (GroceryList. [:cheese]))
(let [gl (GroceryList. [:fish])] (= gl gl))

#_:clj-kondo/ignore
(deftype GroceryList [to-buy]
  Append
  (append [this x]
    (GroceryList. (conj to-buy x)))

  Object
  (toString [this]
    (str "To buy: " to-buy))

  (equals [this other]
    (and (= (type this) (type other))
         (= to-buy (.to-buy other)))))

(= (GroceryList. [:cheese]) (GroceryList. [:cheese]))


;; defrecord 

;; It’d be nice if we could create a type, but have it still work like a map

#_:clj-kondo/ignore
(defrecord GroceryList [to-buy]
  Append
  (append [this x]
    (GroceryList. (conj to-buy x))))

(supers GroceryList)

(GroceryList. [:beans])

(= (GroceryList. [:beans]) (GroceryList. [:beans]))
(= (GroceryList. [:beans]) {:to-buy [:beans]})

(.to-buy (GroceryList. [:bread]))

(get (GroceryList. [:bread]) :to-buy)
(:to-buy (GroceryList. [:bread]))

(-> (GroceryList. [:chicken])
    (assoc :to-buy [:onion])
    (update :to-buy conj :beets))

;; records always carry around an extra map
(assoc (GroceryList. [:tomatoes]) :note "Cherries if possible!")

;; deftype & defrecord produce named types:

(defprotocol Printable
  (print-out [x] "Print out the given object, nicely formatted."))

(extend-protocol Printable
  GroceryList
  (print-out [gl]
    (println "GROCERIES")
    (println "---------")
    (doseq [item (:to-buy gl)]
      (print "[ ] ")
      (print-out item)
      (println)))

  Object
  (print-out [x]
    (print x)))

(print-out (GroceryList. [:cilantro :carrots :pork :baguette]))

(defrecord CountedItem [thing quantity]
  Printable
  (print-out [this]
    (print-out thing)
    (print (str " (" quantity "x)"))))

(print-out (GroceryList. [:cilantro (CountedItem. :carrots 2) :pork :baguette]))

;; When to use 
;; Take a step back, and ask: do I **need** polymorphism here?

;; NOTE: don't
(defrecord Person [name last-name age])

;; NOTE: do
{:name      "Gordon"
 :last-name "Freeman"
 :age       42}

;; you’ll want to use defrecord and deftype when maps aren’t sufficient:
;;  * when you need polymorphism
;;  * when you need to participate in existing protocols or interfaces,
;;  * when multimethod performance is too slow

;; Annotations 

(definterface Foo (foo []))

(deftype ;; annotations on type
    ^{Deprecated true
      Retention RetentionPolicy/RUNTIME
      javax.annotation.processing.SupportedOptions ["foo" "bar" "baz"]
      javax.xml.ws.soap.Addressing {:enabled false :required true}}
    Bar [^int a
         ;; on field
         ^{:tag int
           Deprecated true
           Retention RetentionPolicy/RUNTIME
           javax.annotation.processing.SupportedOptions ["foo" "bar" "baz"]
           javax.xml.ws.soap.Addressing {:enabled false :required true}}
         b]
  ;; on method
  Foo (^{Deprecated true
         Retention RetentionPolicy/RUNTIME
         javax.annotation.processing.SupportedOptions ["foo" "bar" "baz"]
         javax.xml.ws.soap.Addressing {:enabled false :required true}}
       foo [this] 42))

(println (seq (.getAnnotations Bar)))
(println (seq (.getAnnotations (.getField Bar "b"))))
(println (seq (.getAnnotations (.getMethod Bar "foo" nil))))

;; extend-via-metadata

(defprotocol Component
  :extend-via-metadata true
  (start [component]))

(def component
  (with-meta
    {:name "db"}
    {`start (constantly "started")}))

(start component)
