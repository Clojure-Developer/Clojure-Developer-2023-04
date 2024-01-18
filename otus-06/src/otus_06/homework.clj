(ns otus-06.homework
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))

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

;; *** Sales Menu ***
;; ------------------
;; 1. Display Customer Table
;; 2. Display Product Table
;; 3. Display Sales Table
;; 4. Total Sales for Customer
;; 5. Total Count for Product
;; 6. Exit

(defn display-rows
  [rows]
  (doseq [{id :id data :data} rows]
    (printf "%s: %s\n" id data)))

(defn load-table-data
  [table-name]
  (let [raw-rows (->> (str "homework/" table-name ".txt")
                      io/resource
                      slurp
                      str/split-lines
                      (map #(str/split % #"\|")))
        rows (mapv
               (fn [[id & data]] {:id id :data (vec data)})
               raw-rows)
        index (zipmap
                (map :id rows)
                (range))]
    {:rows rows :index index}))

(def read-cust-table (partial load-table-data 'cust))
(def read-prod-table (partial load-table-data 'prod))
(def read-sales-table (partial load-table-data 'sales))

(defn enrich-sales-rows
  [sales-rows customers products with-product-cost?]
  (let [{customers-rows :rows customers-index :index} customers
        {products-rows :rows products-index :index} products]
    (mapv
      (fn [{id :id [cid pid & other] :data}]
        (let [c-idx (get customers-index cid)
              p-idx (get products-index pid)
              c-row (get customers-rows c-idx)
              p-row (get products-rows p-idx)
              c-name (get-in c-row [:data 0])
              p-name (get-in p-row [:data 0])
              p-cost (get-in p-row [:data 1])
              data (if with-product-cost?
                     (into [c-name p-name p-cost] other)
                     (into [c-name p-name] other))]
          {:id id :data data})) sales-rows)))

(defn load-enriched-sales-table
  [with-product-cost?]
  (let [customers (read-cust-table)
        products (read-prod-table)
        sales (read-sales-table)
        sales-rows (enrich-sales-rows (:rows sales) customers products with-product-cost?)]
    (assoc sales :rows sales-rows)))

(def read-enriched-sales-table (partial load-enriched-sales-table false))
(def read-enriched-sales-table-with-product-cost (partial load-enriched-sales-table true))

(defn calc-aggregation
  [reader filter map reduce]
  (let [{rows :rows} (reader)
        aggregation (->> rows filter map reduce)]
    aggregation))

(defn calc-total-sales-for-customer
  [customer-name]
  (calc-aggregation
    read-enriched-sales-table-with-product-cost
    (partial filter #(= customer-name (get-in % [:data 0])))
    (partial map (fn [{[_ _ cost qnt] :data}] (* (parse-double cost) (parse-long qnt))))
    (partial reduce +)))

(defn calc-total-count-for-product
  [target-product-name]
  (calc-aggregation
    read-enriched-sales-table
    (partial filter #(= target-product-name (get-in % [:data 1])))
    (partial map #(parse-long (get-in % [:data 2])))
    (partial reduce +)))

(defn display-table
  [reader]
  (let [{rows :rows} (reader)]
    (display-rows rows)))

(defn display-customer-table []
  (display-table read-cust-table))

(defn display-product-table []
  (display-table read-prod-table))

(defn display-sales-table []
  (display-table read-enriched-sales-table))

(defn display-total-sales-for-customer
  []
  (println "Enter customer name: ")
  (let [customer-name (read-line)]
    (->> customer-name
         calc-total-sales-for-customer
         (format "%s: $%.2f" customer-name)
         println)))
(defn display-total-count-for-product
  []
  (println "Enter product name: ")
  (let [product-name (read-line)]
    (->> product-name
         calc-total-count-for-product
         (format "%s: %d" product-name)
         println)))

(defn goodbye []
  (println "Goodbye")
  (System/exit 0))

(def menu
  [{:name "Display Customer Table" :action display-customer-table}
   {:name "Display Product Table" :action display-product-table}
   {:name "Display Sales Table" :action display-sales-table}
   {:name "Total Sales for Customer" :action display-total-sales-for-customer}
   {:name "Total Count for Product" :action display-total-count-for-product}
   {:name "Exit" :action goodbye}])

(defn print-menu
  [menu]
  (println "*** Sales Menu ***")
  (println "==================")
  (doseq [i (range 0 (count menu))]
    (printf "%s. %s\n" (inc i) (get-in menu [i :name]))))

(defn execute-action
  [menu menu-item-id]
  (let [menu-item (get menu (dec menu-item-id))
        {action :action} menu-item]
    (action)))

(defn choose-menu-item-prompt
  [menu]
  (print-menu menu)
  (println)
  (println "Enter an option?")
  (let [input (read-line)
        parse-result (try
                       (let [menu-item-id (Integer/parseInt input)]
                         [menu-item-id (<= 1 menu-item-id (inc (count menu)))])
                       (catch Exception _ [input false]))]
    (when (false? (second parse-result))
      (println "Wrong menu option:" input "\n"))
    parse-result))

(defn execute-interactive-step
  [menu]
  (let [[id] (->> (repeatedly (partial choose-menu-item-prompt menu))
                  (filter #(true? (second %)))
                  (first))]
    (execute-action menu id)
    (println)))

(defn -main []
  (while true (execute-interactive-step menu)))
