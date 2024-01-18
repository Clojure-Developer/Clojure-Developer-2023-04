(ns otus-06.homework
  (:require 
    [clojure.string :as str]
    [clojure.java.io :as io]
    [clojure.pprint :refer [print-table]]))

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

(defn parse-row
  "Парсит строку таблицы. Параметры:
  - `fields`: список полей таблицы. В качестве элемента списка может выступать
    имя поля (keyword) или пара из имени поля и функции приведения типа.
  - `line`: строка из файла."
  [fields line]
  (into {} (map (fn [field value]
                  (if (vector? field)
                    (let [[field coerce-fn] field]
                      [field (coerce-fn value)])
                    [field value]))
                fields
                (str/split line #"[|]"))))

(defn load-db
  "Читает файл базы данных, возвращает мапу."
  [resource fields]
  (->> resource
       io/resource
       slurp
       str/split-lines
       (map (fn [line]
              (let [row (parse-row fields line)]
                (vector (:id row) row))))
       (into {})))

(defn parse-int
  "Парсит целое число из строки. В случае неудачи возвращает nil."
  [s]
  (try
    (Integer. s)
    (catch java.lang.NumberFormatException _
      nil)))

(defn parse-decimal
  "Парсит десятичную дробь из строки. В случае неудачи возвращает nil."
  [s]
  (try
    (BigDecimal. s)
    (catch java.lang.NumberFormatException _
      nil)))

;; Описания колонок таблиц.
(def cust-columns [:id :name :address :phone])
(def prod-columns [:id :name [:price parse-decimal]])
(def sales-columns [:id :cust-id :prod-id [:quantity parse-int]])

;; Данные таблиц.
(def cust (load-db "homework/cust.txt" cust-columns))
(def prod (load-db "homework/prod.txt" prod-columns))
(def sales (load-db "homework/sales.txt" sales-columns))

(defn get-column-names
  "Возвращает названия колонок."
  [columns]
  (mapv #(if (vector? %) (first %) %)
        columns))

(defn get-table-values
  "Возвращает данные таблиц в виде списка мап."
  [data]
  (->> data (map val) (sort-by :id)))

(defn select-one
  "Возвращает первую строку таблицы, для которой значение указанной колонки
  совпадает с заданным."
  [table column value]
  (some (fn [[_ row]] (when (= value (column row))
                        row))
        table))

(defn sales-list
  "Функция возвращает список продаж, связанных с сущностью из
  соседней таблицы.
  
  - `table`  - таблица, в которой ищем сущность,
  - `name`   - имя, по которому ищем,
  - `forkey` - поле связи в таблице продаж."

  [table name forkey]
  (when-let [table-row (select-one table :name name)]
    (->> sales
         (filter (fn [[_ row]]
                   (= (forkey row) (:id table-row))))
         (map second))))

(defn cust-sales
  "Возвращает общую сумму трат указанного клиента."
  [cust-name]
  (->> (sales-list cust cust-name :cust-id)
       (map (fn [sales-row]
              (* (:quantity sales-row) (-> (:prod-id sales-row) prod :price))))
       (reduce +)))

(defn prod-count
  "Возвращает количество проданных единиц указанного продукта."
  [prod-name]
  (->> (sales-list prod prod-name :prod-id)
       (map :quantity)
       (reduce +)))

(comment
  (select-one cust :name "John Smith")
  (sales-list cust "John Smith" :cust-id)
  (sales-list prod "shoes" :prod-id)
  (cust-sales "John Smith")
  (prod-count "jam"))

(defn show-table
  "Показывает содержимое указанной таблицы. В качестве заголовка использует
  названия колонок данной таблицы."
  [columns table]
  (print-table (get-column-names columns) (get-table-values table)))

(defn show-cust
  []
  (show-table cust-columns cust))

(defn show-prod
  []
  (show-table prod-columns prod))

(defn show-sales
  []
  (show-table sales-columns sales))

(defn show-cust-sales
  "Спрашивает имя покупателя и печатает сумму его покупок."
  []
  (print "Enter customer name: ")
  (flush)
  (when-let [customer (read-line)]
    (println
      (if-let [value (cust-sales customer)]
        (str customer ": " value)
        "Unknown customer."))))

(defn show-prod-count
  "Спрашивает название продукта и печатает проданное количество."
  []
  (print "Enter product name: ")
  (flush)
  (when-let [product (read-line)]
    (println
      (if-let [value (prod-count product)]
        (str product ": " value)
        "Unknown product."))))


(def menu
  {:title "-=[ Sales Menu ]=-"
   :options [{:title "Display Customer Table"   :function show-cust}
             {:title "Display Product Table"    :function show-prod}
             {:title "Display Sales Table"      :function show-sales}
             {:title "Total Sales for Customer" :function show-cust-sales}
             {:title "Total Count for Product"  :function show-prod-count}
             {:title "Exit"                     :function nil}]
   :prompt "\nEnter option: "})

(defn repeat-char
  [n c]
  (str/join "" (repeat n c)))

(defn show-menu
  "Показывает меню, переданное в параметре."
  [menu]
  (->> [[(:title menu)
         (repeat-char (count (:title menu)) "-")]
        (map-indexed (fn [i {title :title}]
               (format "[%d] %s" (inc i) title))
             (:options menu))
        [(:prompt menu)]]
       (apply concat)
       (str/join "\n")
       print)
  (flush))

(defn menu-loop [menu]
  (let [options-len (count (:options menu))]
    (loop []
      ;; Показываем меню.
      (show-menu menu)
      ;; Спрашиваем номер пункта меню у пользователя.
      (let [option (parse-int (read-line))]
        (if (and (some? option)
                 (<= 1 option options-len))
          ;; Если пользователь ввел число и это число соответствует пункту
          ;; меню, выполняем заданное при определении меню действие.
          (let [function (-> menu :options (nth (dec option)) :function)]
            (if function
              (do (function) (println) (recur))
              (println "Program finished.")))
          (do (println "Unknown option.\n") (recur)))))))

(defn -main
  [] (menu-loop menu))

