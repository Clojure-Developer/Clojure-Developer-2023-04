(ns otus-05.rgb
  (:gen-class
   :name otus_05.rgb.Rgb
   :state state
   :init init
   :constructors {[Long Long Long] []}))

(defn -init [r g b]
  [[] [r g b]])
