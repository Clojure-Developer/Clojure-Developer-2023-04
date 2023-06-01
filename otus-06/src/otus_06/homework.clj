(ns otus-06.homework
  (:require [clojure.string :as str]
            [clojure.edn :as edn]
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


(defn get-user-answer
  "Принимает запрос к пользователю и функцию pf, которой проверяется
   ответ пользователя. Если возвращается nil, повторяет запрос"
  [promt pf]
  (loop []
    (println promt)
    (if-let [ans (pf (read-line))]
      ans
      (recur))))

;; ****************************
;; Функции для работы с БД

(def db {
         :customer {
                    :data []
                    :field [:id :name :addres :phone]
                    :field-type [int str str str]
                    :format ["%3d" "%20s" "%-20s" "%10s"]
                    :file "homework/cust.txt"
                    :save-promt [["Name: "] ["Addres: "] ["Phone: "]]}
         :product {:data []
                   :field [:id :description :cost]
                   :field-type [int str double]
                   :format ["%3d" "%20s" "%5.2f"]
                   :file "homework/prod.txt"
                   :save-promt [["Description: "] ["Cost:"]]}
         :sales {
                 :data []
                 :field [:id :customer-id :product-id :count]
                 :field-type [int int int int]
                 :format ["%3d" 
                          [:customer :id :name "%20s"] 
                          [:product :id :description "%10s"] 
                          "%3d"]
                 :save-promt [["Customer: " [:customer :name]] 
                              ["Product: " [:product :description]] 
                              ["Count: "]]
                 :file "homework/sales.txt"}})

(defn get-settings [settings]
  (fn [db table]
    (get-in db [table settings])))
(def get-filename (get-settings :file))
(def get-fieldname (get-settings :field))
(def get-data (get-settings :data))
(def get-format (get-settings :format))
(def get-save-promt (get-settings :save-promt))


(defn read-string* [s]
  ;; Я не нашел как заставить read-string 
  ;; корректно прочитать "123-456" "123 jonh" и т.п.
  ;; Решил обойти таким образом. 
  ;; Буду благодарен за подсказку
  (if (re-matches #"\d+\.*\d*"  s)
    (edn/read-string s)
    s))
  
(defn create-row 
  "Делает из строки или суквенции мапу для вставки в БД"
  [line fields]
  (->> (if (string? line)
         (->> line
              (#(str/split % #"\|"))
              (map read-string*))
         line)
      (zipmap fields)))

(defn add-row
  "Добавляет строку в БД"
  [table db line]
  (update-in db
             [table :data]
             conj
             (create-row line (get-fieldname db table))))
  

(defn load-table 
  "Читает данные из файла и загружает их в таблицу" 
  [db table]
  (with-open [f (-> (get-filename db table)
                    (io/resource)
                    (io/reader))]
    (->> f
         line-seq
         (reduce (partial add-row table) db))))

(defn load-db 
  "Загружает всю БД"
  [db]
  (reduce load-table db (keys db)))

(defn find-row 
  "Ищет строки в которых field = x"
  [db table field x]
  (filterv #( = x (field %))(get-data db table)))

(defn find-first-row 
  "Ищет первую строку, попадающую под условия"[db table field x]
  (first (find-row db table field x)))
  
(defn format-field 
  "Форматирует поле таблицы в соответсвии с заданным форматом.
   Заодно подгружает название из других таблиц, если есть справочник"
  [db]
  (fn [fmt s]
    (if (vector? fmt)
      (let [[tbl fld name-field fs] fmt
            name (name-field(find-first-row db tbl fld s))]
        (format fs name))
      (format fmt s))))

(defn show-table 
  "Отображает всю таблицу.
   Основная функция для входа из меню"
  [db table]
  (let [format-str (get-format db table)]
    (doseq [row (get-data db table)]
      (->> (map row (get-fieldname db table))
           (map (format-field db) format-str)
           (str/join " | ")
           println))
    (println "Enter to continue..")
    (read-line)
    db))

(defn nothing 
  "Ничего не делает"
  [& args]
  (first args))

;;*************************************
;; Функции для работы с отчетом

(def reports {:cost-by-user
              {:data [* [[:product-id [:product :cost]] [:count]]]
               :groupby [:customer-id [:customer :id :name]]
               :format ["\nEnter user name: " "%s: %5.2f\n"]}
              :prod-count
              {:data [nothing [[:count]]]
               :groupby [:product-id [:product :id :description]]
               :format ["\nEnter product name: " "%s: %3.0f\n"]}})
              
(defn get-rep-exp [rep]
  (get-in reports [rep :data]))
(defn get-rep-groupby [rep]
  (get-in reports [rep :groupby]))
(defn get-rep-format [rep]
  (get-in reports [rep :format]))


(defn get-val 
  "Получает данные в соответсвии с exp,
   опирается при этом на row"
  [db exp row]
  (if (= 1 (count exp))
    ((first exp) row)
    (let [[f-id [tbl fld]] exp]
      (fld (find-first-row db tbl :id (f-id row))))))
  
(defn calc-report
  "Вычисляет значение, необходимое для отчета"
  [db rep name]
  (let [[f args] (get-rep-exp rep)
        [f-group [t-search f-id f-search]] (get-rep-groupby rep)]
       (if-let [id (f-id (find-first-row db t-search f-search name))]
         (let [rows (find-row db :sales f-group id)
               arg-rows (for [r rows]
                         (map #(get-val db % r) args))]
           (* (apply + (map #(apply f %) arg-rows)) 1.0))
         0.0)))

(defn show-report 
  "Выводит отчет. Основная для входа из менб"
  [db rep]
  (let [[promt, fmt] (get-rep-format rep)]
    (println promt)
    (let [name (read-line)]
      (println (format fmt name (calc-report db rep name)))))
  db)
;;*************************************
;; Функции для добавления записей

(defn create-string 
  "Принимает вектор значений и возвращает строку, как в файле"
  [fields row]
  (->> (map row fields)
       (map str)
       (str/join "|")))

(defn save-table 
  "Сохраняет таблицу в файл"
  [db table]
  (spit 
   (io/resource (get-filename db table))
   (let [fields (get-fieldname db table)]
     (->> (get-data db table)
          (map (partial create-string fields))
          (str/join "\n"))))
  db)

(defn promt-cell 
  "Запрашивает у пользователя ввод очередного поля.
   Проверяет его на допустимость и возвращает нужное значение"
  [acc exp]
  (let [[db _] acc
        [promt [table field]] exp]
    (if (nil? table)
      (get-user-answer promt nothing)
      (get-user-answer promt
                       #(:id (find-first-row db table field %))))))

(defn add-cell 
  "Добавляет новую ячейку в строку"
  [acc exp]
  (update-in acc [1] conj (promt-cell acc exp)))
   

(defn promt-row 
  "Запрашивает все данные для новой записи"
  [db table]
  (let [max-id (apply max (map :id (get-data db table)))
        row (reduce add-cell [db [(str (inc max-id))]] (get-save-promt db table))]
    (-> (row 1)
        (#(str/join "|" %)))))


(defn new-row 
  "Реализует пункт меню с добавлением записей"
  [db table]
  (->> (promt-row db table)
       (add-row table db)
       (#(save-table % table)))) 

  
;;*************************************
;; Функции для работы с меню

(defn exit [_]
  (System/exit 0))

(def menu (into 
           (sorted-map)
           {"1" {:name-menu "Display Customer Table"
                 :func #(show-table % :customer)}
            "2" {:name-menu "Display Product Table"
                 :func #(show-table % :product)}
            "3" {:name-menu "Display Sales Table"
                 :func #(show-table % :sales)}
            "4" {:name-menu "Total Sales for Customer"
                 :func #(show-report % :cost-by-user)}
            "5" {:name-menu "Total Count for Product"
                 :func #(show-report % :prod-count)}
            "6" {:name-menu "New Customer"
                 :func #(new-row % :customer)}
            "7" {:name-menu "New Product"
                 :func #(new-row % :product)}
            "8" {:name-menu "New Sale"
                 :func #(new-row % :sales)}
            "9" {:name-menu "Exit"
                 :func exit}}))

(defn check-menu [answer]
  (get-in menu [answer :func]))

(defn show-menu 
  "Выводит меню и просит сделать выбор" 
  []
  (println "*** MAIN MENU ***")
  (doseq [[k v] menu]
    (println k " - " (:name-menu v)))
  (get-user-answer "Enter your choice: " check-menu))
  
(defn -main 
  "Точка входа. Крутит меню и запускает нужные функции"
  []
  (reduce #(%2 %1) (load-db db) (repeatedly show-menu)))

;; (-main)
