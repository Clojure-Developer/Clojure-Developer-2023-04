(ns otus-06.homework
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

;; Загрузить данные из трех файлов на диске.
;; Эти данные сформируют вашу базу данных о продажах.
;; Каждая таблица будет иметь «схему», которая указывает поля внутри.
;; Итак, ваша БД будет выглядеть так:

;; cust.txt: это данные для таблицы клиентов. Схема:
;; <custID, name, address, phoneNumber>

;; Примером файла cust.txt может быть:
;; 1|John Smith|123 Here Street|456-4567
;; 2|Sue Jones|43 Rose Court Street|345-7867
;; 3|Fan Yuhong|165 Happy Lane|345-4533

;; Каждое поле разделяется символом «|». и содержит непустую строку.

;; prod.txt: это данные для таблицы продуктов. Схема
;; <prodID, itemDescription, unitCost>

;; Примером файла prod.txt может быть:
;; 1|shoes|14.96
;; 2|milk|1.98
;; 3|jam|2.99
;; 4|gum|1.25
;; 5|eggs|2.98
;; 6|jacket|42.99

;; sales.txt: это данные для основной таблицы продаж. Схема:
;; <salesID, custID, prodID, itemCount>.
;;
;; Примером дискового файла sales.txt может быть:
;; 1|1|1|3
;; 2|2|2|3
;; 3|2|1|1
;; 4|3|3|4

;; Например, первая запись (salesID 1) указывает, что Джон Смит (покупатель 1) купил 3 пары обуви (товар 1).

;; Задача:
;; Предоставить следующее меню, позволяющее пользователю выполнять действия с данными:

;; *** Sales Menu ***
;; ------------------
;; 1. Display Customer Table
;; 2. Display Product Table
;; 3. Display Sales Table
;; 4. Total Sales for Customer
;; 5. Total Count for Product
;; 6. Exit

;; Enter an option?


;; Варианты будут работать следующим образом

;; 1. Вы увидите содержимое таблицы Customer. Вывод должен быть похож (не обязательно идентичен) на

;; 1: ["John Smith" "123 Here Street" "456-4567"]
;; 2: ["Sue Jones" "43 Rose Court Street" "345-7867"]
;; 3: ["Fan Yuhong" "165 Happy Lane" "345-4533"]

;; 2. То же самое для таблицы prod.

;; 3. Таблица продаж немного отличается.
;;    Значения идентификатора не очень полезны для целей просмотра,
;;    поэтому custID следует заменить именем клиента, а prodID — описанием продукта, как показано ниже:
;; 1: ["John Smith" "shoes" "3"]
;; 2: ["Sue Jones" "milk" "3"]
;; 3: ["Sue Jones" "shoes" "1"]
;; 4: ["Fan Yuhong" "jam" "4"]

;; 4. Для варианта 4 вы запросите у пользователя имя клиента.
;;    Затем вы определите общую стоимость покупок для этого клиента.
;;    Итак, для Сью Джонс вы бы отобразили такой результат:
;; Sue Jones: $20.90

;;    Это соответствует 1 паре обуви и 3 пакетам молока.
;;    Если клиент недействителен, вы можете либо указать это в сообщении, либо вернуть $0,00 за результат.

;; 5. Здесь мы делаем то же самое, за исключением того, что мы вычисляем количество продаж для данного продукта.
;;    Итак, для обуви у нас может быть:
;; Shoes: 4

;;    Это представляет три пары для Джона Смита и одну для Сью Джонс.
;;    Опять же, если продукт не найден, вы можете либо сгенерировать сообщение, либо просто вернуть 0.

;; 6. Наконец, если выбрана опция «Выход», программа завершится с сообщением «До свидания».
;;    В противном случае меню будет отображаться снова.


;; *** Дополнительно можно реализовать возможность добавлять новые записи в исходные файлы
;;     Например добавление нового пользователя, добавление новых товаров и новых данных о продажах


;; Файлы находятся в папке otus-06/resources/homework

(def cust-file "resources/homework/cust.txt")
(def prod-file "resources/homework/prod.txt")
(def sales-file "resources/homework/sales.txt")

(def table-schema
  {:cust {:file cust-file :id 0 :name 1 :address 2 :phone 3}
   :prod {:file prod-file :id 0 :name 1 :cost 2}
   :sales {:file sales-file :id 0 :cust-id 1 :prod-id 2 :count 3}})

(defn map-line [line schema]
  (into {} (for [[key value] (dissoc schema :file)]
             [key (nth line value)])))

(defn clear-terminal []
  (print "\033c"))

(defn parse-line [line]
  (str/split line #"\|"))

(defn for-each-line [file cb]
  (with-open [rdr (io/reader file)]
    (doseq [[index line] (map-indexed vector (line-seq rdr))]
      (let [line (parse-line line)]
          (cb line index)))))

(defn format-row [row index]
  (->> row
      (mapv #(format "\"%s\"" %))
      (str/join ", ")
      (format "%d: [%s]" (inc index))))

(defn print-table [file]
  (for-each-line file (fn [line index]
                        (-> (format-row line index)
                            (println)))))

(defn load-table [table]
  (let [{:keys [file] :as schema} (table table-schema)
        all-ids (atom [])
        by-id (atom {})]
    (for-each-line file (fn [line _] ;; do we have a way to pass callback with only one argument?
                          (let [[id] line
                                data (map-line line schema)]
                            (swap! all-ids conj id)
                            (swap! by-id assoc id data))))
    {:all-ids @all-ids
     :by-id @by-id}))

(defn load-tables []
  {:cust (load-table :cust)
   :prod (load-table :prod)
   :sales (load-table :sales)})

(defn find-by-id [db table id]
  (get-in db [table :by-id id]))

(defn find-by-name [db table name]
  (let [table (get-in db [table :by-id])
        [[id data]] (filter (fn [[_key value]] (= name (:name value))) table)]
    {:id id :data data}))

(defn calc-total-sales-for-customer [db cust-id]
  (let [sales (get-in db [:sales :by-id])
        sales-to-customer (filter (fn [[_key value]] (= cust-id (:cust-id value))) sales)]
    (reduce (fn [acc [_key value]]
              (let [count (Integer/parseInt (get value :count "0"))
                    product (get-in db [:prod :by-id (:prod-id value)])
                    cost (Float/parseFloat (get product :cost "0.00"))]
                (+ acc (* count cost))))
            0 sales-to-customer)))

(defn calc-total-count-for-product [db prod-id]
  (let [sales (get-in db [:sales :by-id])
        product-sales (filter (fn [[_key value]] (= prod-id (:prod-id value))) sales)]
    (reduce (fn [acc [_key value]]
              (let [count (Integer/parseInt (get value :count "0"))]
                (+ acc count)))
            0 product-sales)))

(defn on-exit [running?]
 (println "Goodbye")
 (dosync (ref-set running? false)))

(defn on-display-customer-table []
  (print-table cust-file))

(defn on-display-product-table []
  (print-table prod-file))

(defn on-display-sales-table []
  (let [db (load-tables)]
    (doseq [[index id] (map-indexed vector (get-in db [:sales :all-ids]))
            :let [{:keys [cust-id prod-id count]} (find-by-id db :sales id)
                  {cust-name :name} (find-by-id db :cust cust-id)
                  {prod-name :name} (find-by-id db :prod prod-id)]]
      (println (format-row [cust-name prod-name count] index)))))

(defn on-display-total-sales-for-customer []
 (let [db (load-tables)]
  (println "Enter customer name:")
  (let [customer-name (read-line)
        {customer-id :id customer :data} (find-by-name db :cust customer-name)]
    (if customer
      (let [total (calc-total-sales-for-customer db customer-id)]
        (println (format "Total Sales for Customer %s: $%.2f" customer-name total)))
     (println (format "Customer %s not found" customer-name))))))

(defn on-display-total-count-for-product []
  (let [db (load-tables)]
    (println "Enter product name:")
    (let [product-name (read-line)
          {product-id :id product :data} (find-by-name db :prod product-name)]
      (if product
        (let [count (calc-total-count-for-product db product-id)]
          (println (format "Total Count of Product %s: %d" product-name count)))
        (println (format "Product %s not found" product-name))))))

(defn render-menu [menu]
 (clear-terminal)
 (println "*** Sales Menu ***")
 (println "==================")
 (doseq [[index item] (map-indexed vector menu)]
  (println (str (inc index) ". " (:title item))))
 (println "Enter an option? "))

(defn get-user-input []
  (let [input (read-line)]
    (try
      (Integer/parseInt input)
      (catch Exception e
        (println "Invalid input")
        -1))))

(defn start-app
 "Displaying main menu and processing user choices."
  []
  (let [running? (ref true)
        menu [{:title "Display Customer Table" :action on-display-customer-table}
              {:title "Display Product Table" :action on-display-product-table}
              {:title "Display Sales Table" :action on-display-sales-table}
              {:title "Total Sales for Customer" :action on-display-total-sales-for-customer}
              {:title "Total Count for Product" :action on-display-total-count-for-product}
              {:title "Exit" :action (partial on-exit running?)}]]
    (render-menu menu)
    (while @running?
      (let [choice (get-user-input)
            item (get menu (dec choice))
            action (get item :action (fn []))]
        (action)))))

(defn -main
     "Main function calling app."
     [& _args]
     (start-app))
