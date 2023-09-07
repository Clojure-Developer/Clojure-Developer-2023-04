(ns otus-16.homework
  (:require [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.net URLDecoder]
           [java.nio.charset StandardCharsets]
           [java.lang IllegalArgumentException]))

(def chunk-size 8192)

;; Регулярное выражения для парсинга COMBINED-логов. Именованные группы
;; здесь чисто ради удобства.
(def combined-log-re
  (re-pattern
    (str "^" (str/join "\\s+" ["(?<host>\\S+)"
                               "-"
                               "(?<user>\\S+)"
                               "\\[(?<time>[^\\]]+)\\]"
                               "\"(?<method>\\S+)\\s+(?<url>\\S+)\\s+HTTP/(?<httpver>\\d+\\.\\d+)\""
                               "(?<status>\\d{3})"
                               "(?<size>\\d+|-)"
                               "\"(?<referrer>[^\"]+)\""
                               "\"(?<useragent>[^\"]+)\""])
         ".*")))

;; Формат времени, используемый в COMBINED-логах.
(def combined-log-time-format "dd/MMM/yyyy:HH:mm:ss ZZZZZ")

;; Описание параметров, которые мы будем извлекать из логов.
;; - `:init` - начальное значение,
;; - `:acc-fn` - функция для выделения значения из результатов парсинга
;;               строки и добавления этого значения к итогам.
;; - `:merge-fn` - функция для объединения результатов парсинга
;;                 нескольких чанков.
(def slices {;; Подсчет общего числа переданных байт.
             :total-bytes
             {:init 0
              :acc-fn (fn [old log-record]
                        (+ old (or (:size log-record) 0)))
              :merge-fn +}
             ;; Подсчет количества переданных байт для каждого URL.
             :bytes-by-url
             {:init {}
              :acc-fn (fn [old log-record]
                        (update old (:url log-record) (fnil + 0) (or (:size log-record) 0)))
              :merge-fn (fn [old new]
                          (merge-with + old new))}
             ;; Подсчет количества URL для каждого Referrer.
             :urls-by-referrer
             {:init {}
              :acc-fn (fn [old log-record]
                        (update old (:referrer log-record) (fnil inc 0)))
              :merge-fn (fn [old new]
                          (merge-with + old new))}
             })

(defn parse-time
  "Функция для парсинга времени из строки."
  [s]
  (.parse (java.text.SimpleDateFormat. combined-log-time-format) s))

(defn urldecode
  "Декодирует URL-закодированную строку."
  [url]
  (when url
    (try (URLDecoder/decode url StandardCharsets/UTF_8)
         (catch IllegalArgumentException _
           url))))

(defn parse-combined-log-line
  "Функция для парсинга строки лога в COMBINED формате."
  [line]
  (when-let [m (re-matches combined-log-re line)]
    (->> 
      ;; Сначала преобразуем распаршенную строку логов в набор пар ключ-значение.
      ;; Проще использовать zipmap, чем заморачиваться с именованными группами matcher'а.
      (zipmap [:host :user :time :method :url :http-ver :status :size :referrer :user-agent]
              (rest m))
      ;; Находим поля с прочерком и заменяем их на nil.
      (map (fn [[k v]]
             (vector k (if (and (k #{:user :size :referrer :user-agent})
                                (= v "-"))
                         nil
                         v))))
      ;; Преобразуем некоторые значения в числа.
      (map (fn [[k v]]
             (vector k (if (and (k #{:status :size})
                                (not (nil? v)))
                         (Integer/parseInt v)
                         v))))
      ;; Преобразуем в словарь.
      (into {})
      (#(-> %
            ;; Декодируем URL.
            (update :url urldecode)
            ;; И Referrer.
            (update :referrer urldecode)
            ;; Заменяем строку с временем на значение времени.
            (update :time parse-time))))))

(defn get-init
  "Возвращает мапу с начальными значениями извлекаемых из логов параметров."
  [slices]
  (into {} (map (fn [[k {init :init}]] [k init]) slices)))


(defn parse-chunk 
  "Парсит несколько строк лога (чанк).

  - `acc`        - аккумулятор для аггрегации данных,
  - `slices`     - описание данных, которые будут извлекаться из логов,
  - `conditions` - набор значений для фильтрации записей, при парсинге
    будут отбираться только те записи, у которых значения полей соответствуют
    значениям, переданным в `conditions`.
  - `chunks`     - список строк для парсинга."
  [slices conditions chunk]
  (reduce (fn [data line]
            (let [log-record (parse-combined-log-line line)]
              ;; Обновляем данные только если поля записи совпадают с 
              ;; заданными в conditions.
              (if (every? (fn [[k v]]
                            (= (k log-record) v)) conditions)
                ;; Обновляем все параметры, переданные в slices.
                (reduce (fn [data [slice-name {acc-fn :acc-fn}]]
                          (update data slice-name acc-fn log-record))
                        data slices)
                data)))
          (get-init slices)
          chunk))

(defn merge-chunks
  "Объединяет итоги в один общий итог. Названия параметров и функции для их
  аггрегации передаются в мапе `slices`."
  [slices chunks]
  (reduce (fn [chunks-merged chunk-next]
            (reduce (fn [chunks-merged [name {merge-fn :merge-fn}]]
                      (update chunks-merged name merge-fn (chunk-next name)))
                    chunks-merged
                    slices))
          chunks))

(defn parse-file
  "Парсит файл логов и возвращает итог. Названия параметров и функции для их
  аггрегации передаются в мапе `slices`."
  [filename chunk-size slices conditions]
  (with-open [reader (io/reader filename)]
    (let [chunks (partition-all chunk-size (line-seq reader))]
      (merge-chunks slices (pmap (partial parse-chunk slices conditions) chunks)))))

(defn parse-files
  [filenames chunk-size slices conditions]
  (merge-chunks slices (map #(parse-file % chunk-size slices conditions) filenames)))

(defn solution [files & {:keys [] :as conditions}]
  (parse-files files chunk-size slices conditions))

(comment
  (time
    (let [filenames (->> (clojure.java.io/file "/path/to/logs/")
                         file-seq
                         (filter #(.isFile %))
                         (map str))]
      (solution filenames)
      #_(solution filenames {:url "/.ssh/id_rsa"})
      #_(solution filenames {:referrer nil})
      #_(solution filenames {:url "/.ssh/id_rsa" :referrer nil})
      )))
