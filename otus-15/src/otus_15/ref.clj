(ns otus-15.ref)

(defn character
  [name & {:as opts}]
  (ref (merge {:name name
               :items #{}
               :health 500}
              opts)))

(def smaug
  (character "Smaug"
             :health 500
             :strength 400
             :items (set (range 50))))

(def bilbo
  (character "Bilbo"
             :health 100
             :strength 100))

(def gandalf
  (character "Gandalf"
             :health 75
             :mana 750))

(defn loot
  [from to]
  (dosync
   (when-let [item (-> @from :items first)]
     (alter to update :items conj item)
     (alter from update :items disj item))))

(do
  (future (while (loot smaug bilbo)))
  (future (while (loot smaug gandalf))))

@smaug
@bilbo
@gandalf

[(-> @bilbo :items count) (-> @gandalf :items count)]
(filter (:items @bilbo) (:items @gandalf))

(defn flawed-loot
  [from to]
  (dosync
   (when-let [item (-> @from :items first)]
     (commute to update :items conj item)
     (commute from update :items disj item))))

(do
  (future (while (flawed-loot smaug bilbo)))
  (future (while (flawed-loot smaug gandalf))))

[(-> @bilbo :items count) (-> @gandalf :items count)]
(filter (:items @bilbo) (:items @gandalf))

(defn fixed-loot
  [from to]
  (dosync
   (when-let [item (-> @from :items first)]
     (commute to update :items conj item)
     (alter from update :items disj item))))

(do
  (future (while (fixed-loot smaug bilbo)))
  (future (while (fixed-loot smaug gandalf))))

[(-> @bilbo :items count) (-> @gandalf :items count)]
(filter (:items @bilbo) (:items @gandalf))

(defn attack
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1)
                   (:strength @aggressor))]
     (commute target update :health (fn [target-health]
                                      (max 0 (- target-health damage)))))))

(def alive? (comp pos? :health))

(defn play
  [character action target]
  (while (and (alive? @character)
              (alive? @target)
              (action character target))
    (Thread/sleep (rand-int 50))))

(do (future (play bilbo attack smaug))
    (future (play smaug attack bilbo)))

[(:health @smaug) (:health @bilbo)]

(def daylight (ref 1))

(defn attack-with-light
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1)
                   (:strength @aggressor)
                   @daylight)]
     (commute target update :health (fn [target-health]
                                      (max 0 (- target-health damage)))))))

(defn attack-with-light*
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1)
                   (:strength @aggressor)
                   (ensure daylight))]
     (commute target update :health (fn [target-health]
                                      (max 0 (- target-health damage)))))))
