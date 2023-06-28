(ns otus-16.homework)



(defn solution [& {:keys [url referrer]
                   :or   {url :all referrer :all}}]
  (println "doing something")
  {:total-bytes      12345
   ;; если указан параметр url, то в хэш-мапе будет только одно значение
   :bytes-by-url     {"some-url" 12345}
   ;; если указан параметр referrer, то в хэш-мапе будет только одно значение
   :urls-by-referrer {"some-referrer" 12345}})



(comment
 ;; возможные вызовы функции
 (solution)
 (solution :url "some-url")
 (solution :referrer "some-referrer")
 (solution :url "some-url" :referrer "some-referrer"))
