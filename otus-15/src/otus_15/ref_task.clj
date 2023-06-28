(ns otus-15.ref-task)

(def bilbo
  (ref {:name "Bilbo", :health 100, :strength 100}))

(def smaug
  (ref {:name "Smaug", :health 500, :strength 400}))

(def gandalf
  (ref {:name "Gandalf", :health 75, :mana 750}))

(defn heal
  [healer target]
  (dosync (let [health-delta (* (rand 0.1) (:mana @healer))
                mana-delta (max 5 (/ health-delta 5))]
            (when (pos? health-delta)
              (alter healer update :mana - mana-delta)
              (commute target update :health + health-delta)))))

(do (future (while (heal gandalf bilbo)
              (Thread/sleep (rand-int 50))))
    (future (while (heal gandalf smaug)
              (Thread/sleep (rand-int 50)))))

[(:mana @gandalf) (:health @bilbo) (:health @smaug)]
