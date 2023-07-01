(ns otus-10.homework
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(def id3v2-format 
  "Верхний уровень описания формата id3v2."
  {:name :id3v2 :type :id3v2})

(def id3v2-types
  "Описания типов, используемых в id3v2."
  {:id3v2
   {:type :seq
    :seq [{:name :header    :type :header}
          {:name :header-ex :type :header-ex
           ;; Элемент присутствует только если элемент по указанному в ключе
           ;; :if пути истиннен.
           :if [:id3v2 :header :flags :header-ex]}
          {:name :frames    :type :repeat
           :repeat {:type :frame}
           ;; Элементы типа :repeat будут повторяться, пока функция возвращает истину.
           :repeat-while-fn (fn [parsed _path]
                              ;; Парсим фреймы, пока не выйдем за пределы заголовка или пока не 
                              ;; получим невалидный фрейм.
                              (and (< (get parsed :-offset) (get-in parsed [:id3v2 :header :size]))
                                   (let [frames (get-in parsed [:id3v2 :frames])]
                                     (or (empty? frames) (:valid (last frames))))))}]}
   :header
   {:type :seq
    :seq [{:name :magic            :type :magic :contents "ID3"}
          {:name :version-major    :type :u1}
          {:name :version-revision :type :u1}
          {:name :flags            :type :flags :flags [:unsync :header-ex :footer]}
          {:name :size             :type :size}]}
   :header-ex
   {:type :seq
    :seq [{:name :size         :type :size}
          {:name :flags-size   :type :u1}
          {:name :flags        :type :flags
           :flags [:reserved :update :crc :restrictions]}
          {:name :crc          :type :crc
           :if [:id3v2 :header-ex :flags :crc]}
          {:name :restrictions :type :bit-values
           :if [:id3v2 :header-ex :flags :restrictions]
           :values [[:tag-size 2]
                    [:text-encoding 1]
                    [:text-size 2]
                    [:image-encoding 1]
                    [:image-size 2]]}
          {:name :padding :type :data
           ;; Размер поля типа :data определяется результатом выполнения функции.
           ;; В данном случае это количество неиспользованных байт в расширенном заголовке.
           :size-fn (fn [parsed _path]
                      (let [header-ex (get-in parsed [:id3v2 :header-ex])]
                      (- (:size header-ex) 4 1 (:flags-size header-ex)
                         (if (get-in header-ex [:flags :crc]) 5 0)
                         (if (get-in header-ex [:flags :restrictions]) 1 0))))}]}
   :frame
   {:type :seq
    :seq [{:name :id   :type :str :length 4}
          {:name :size :type :size}
          {:name :flags-status :type :flags
           :flags [:reserved :tag-preserve :file-preserve :read-only]}
          {:name :flags-format :type :flags
           :flags [:reserved :group-identity :reserved :reserved :compression :encryption :unsync :data-length-indicator]}
          {:name :data :type :data
           :size-fn (fn [parsed path]
                      (get-in parsed (conj path :size)))}
          ;; Вычисляемое поле, которое будет выставлено в false, если мы получим невалидный
          ;; фрейм во время парсинга.
          {:name :valid :type :instance
           :value-fn (fn [parsed path]
                       (not= (get-in parsed (conj path :id))
                             (str/join (repeat 4 (char 0)))))}]}})

(defn get-bytes
  "Функция читает из потока указанное число байт, возвращает byte-array."
  [in n]
  (let [bs (byte-array n)
        nr (.read in bs)]
    ;; Преобразуем в беззнаковые числа.
    (if (= n nr) (mapv (partial bit-and 16rFF) bs)
      nil)))

(defn get-byte
  "Функция читает из потока 1 байт и возвращает его."
  [in]
  (first (get-bytes in 1)))

;;
;; Парсинг базовых типов.
;;

(defn resolve-type
  "Ресолвер для мультиметода, который используется при парсинге id3v2.
  
  - `in`     - входящий поток,
  - `parsed` - рапаршенные на текущий момент данные,
  - `path`   - путь к текущему элементу в мапе `parsed`,
  - `types`  - доступные парсеру типы,
  - `fmt`    - формат текущего элемента."
  [_in _parsed _path _types fmt]
  (:type fmt))

(defmulti parse-type #'resolve-type)

(defn check-condition
  "Функция для проверки условия опциональных элементов.

  Условие может быть указано либо в виде пути (ключ :if), в 
  этом случае опциональный элемент будет присутствовать, если
  значение элемента по указанному пути истинно.
  
  Также условие может быть представлено в виде функции, на вход
  которой передается мапа распаршенных на данный момент элементов."
  [parsed fmt]
  (cond
    (contains? fmt :if) (get-in parsed (:if fmt))
    (contains? fmt :if-fn) ((:if-fn fmt) parsed)
    :else true))

(defmethod parse-type :u1
  [in parsed path _types fmt]
  "Метод для парсинга базового типа: байта."
  ;; При парсинге мы обновляем текущее смещение (ключ :-offset)
  ;; и записываем прочитанное значение элемента по указанному пути.
  (assoc-in (update parsed :-offset inc)
            (conj path (:name fmt))
            (get-byte in)))

(defmethod parse-type :magic
  [in parsed path _types fmt]
  "Метод для парсинга базового типа: магической строки.
  Если значение не соответствует заданному в ключе :contents, метод
  выбрасывает исключение."
  (let [magic-val (:contents fmt)
        magic-len (count (:contents fmt))
        bytes-val (->> (get-bytes in magic-len) (map char) str/join)]
    (if (= magic-val bytes-val)
      (assoc-in (update parsed :-offset + magic-len)
                (conj path (:name fmt))
                bytes-val)
      (throw (Exception. "Incorrect magic value.")))))

(defmethod parse-type :flags
  [in parsed path _types fmt]
  "Метод для парсинга базового типа: флагов. Имена флагов задаются
  в ключе :flags, который представляет из себя список флагов от
  старшего бита к младшему."
  (->> (get-byte in)
       Integer/toBinaryString
       Integer/parseInt
       (format "%08d")
       (map #(= % \1))
       (zipmap (:flags fmt))
       (into {})
       (assoc-in (update parsed :-offset inc)
                 (conj path (:name fmt)))))

(defn parse-syncsafe
  "Функция для парсинга syncsafe значений. Такие значения представляют
  себя последовательность байтов, в которых игнорируется 7й бит."
  [in parsed path fmt n]
  (->> (get-bytes in n)
       reverse
       (map-indexed (fn [i b]
                      (bit-shift-left (bit-and b 16r7f) (* 7 i))))
       (reduce +)
       (assoc-in (update parsed :-offset + n)
                 (conj path (:name fmt)))))

(defmethod parse-type :size
  [in parsed path _types fmt]
  "Метод для парсинга базового типа: размера."
  (parse-syncsafe in parsed path fmt 4))

(defmethod parse-type :crc
  [in parsed path _types fmt]
  "Метод для парсинга базового типа: контрольной суммы."
  (parse-syncsafe in parsed path fmt 5))

(defmethod parse-type :bit-values
  [in parsed path _types fmt]
  "Метод для парсинга базового типа: битовых значений. Описания
  значений хранятся в ключе :values и представляют из себя пары
  имя параметра - количество бит."
  (let [bit-string (->> (get-byte in)
                        Integer/toBinaryString
                        Integer/parseInt
                        (format "%08d"))]
    (->>
      (reduce (fn [[res bs] [k n]]
                [(conj res [k (-> (take n bs) str/join (Integer/parseInt 2))])
                 (drop n bs)])
              [[] bit-string]
              (:values fmt))
      first
      (into {})
      (assoc-in (update parsed :-offset inc)
                (conj path (:name fmt))))))

(defmethod parse-type :data
  [in parsed path _types fmt]
  "Метод для парсинга базового типа: произвольных данных.
  Это просто набор байт, чья длина описывается результатом выполнения
  функции в ключе :size-fn."
  (let [size ((:size-fn fmt) parsed path)]
    (->> (get-bytes in size)
         (assoc-in (update parsed :-offset + size)
                   (conj path (:name fmt))))))

(defmethod parse-type :str
  [in parsed path _types fmt]
  "Метод для парсинга базового типа: ASCII строки. Длина строки
  описывается ключом :length."
  (->> (get-bytes in (:length fmt))
       (map char)
       str/join
       (assoc-in (update parsed :-offset + (:length fmt))
                 (conj path (:name fmt)))))

(defmethod parse-type :instance
  [_in parsed path _types fmt]
  "Метод для добавления произвольного элементов. Значение параметра вычисляется
  на основе уже распаршенных элементов с помощью функции, переданной в ключе :value-fn."
  (assoc-in parsed
            (conj path (:name fmt))
            ((:value-fn fmt) parsed path)))


(defmethod parse-type :seq
  [in parsed path types fmt]
  "Метод для парсинга базового типа: последовательности элементов.
  Последовательности объединяют другие элементы в цепочки. Результатом парсинга
  будет мапа (а не список, т.е. порядок последовательности после парсинга
  не сохраняется)"
  (if-let [fmt-seq (:seq fmt)]
    (reduce (fn [p f]
              ;; Для необязательных элементов проверяем, нужно ли их парсить.
              (if (check-condition p f)
                (parse-type in p path types f)
                p))
            parsed
            fmt-seq)
    ;; Выкидываем исключение, если в типе "seq" не указана последовательность.
    (throw (Exception. (str "Sequence not found: " fmt)))))

(defmethod parse-type :repeat
  [in parsed path types fmt]
  "Метод для парсинга базового типа: повторяющихся элементов. Элементы будут
  парситься, пока истинно значение функции из ключа :repeat-while-fn. Тип элементов
  описывается ключом :repeat-type.
  Результатом парсинга будет вектор."
  (let [repeat-while-fn (:repeat-while-fn fmt)
        path (conj path (:name fmt))]
    (loop [parsed (assoc-in parsed path [])
           i 0]
      (if (repeat-while-fn parsed path)
        (recur (parse-type in parsed path types (assoc (:repeat fmt) :name i))
               (inc i))
        parsed))))

(defmethod parse-type :default
  [in parsed path types fmt]
  "Метод парсинга по умолчанию. Используется, если ни один из базовых типов не
  подошел. В этом случае метод попытается найти тип в списке составных типов,
  а в случае неудачи - выкинет исключение."
  (if (check-condition parsed fmt)
    (if-let [fmt-type (:type fmt)]
      (parse-type in parsed (conj path (:name fmt)) types (types fmt-type))
      ;; Выкидываем исключения, если тип не найден в списке типов.
      (throw (Exception. (str "Format error: " fmt))))
    parsed))

;;
;; Парсинг текста.
;;

(defn resolve-text
  "Ресолвер для текстовых тегов."
  [data]
  ;; Ориентируемся на первый байт данных.
  (first data))

(defmulti parse-text #'resolve-text)

(defn decode-bytes
  "Преобразует список байт в тест в указанной кодировке."
  [data encoding]
  (String. (byte-array (count data) data) encoding))

(defmethod parse-text 0
  [data]
  (decode-bytes (rest data) "ISO-8859-1"))

(defmethod parse-text 1
  [data]
  (decode-bytes (rest data) "UTF-16"))

(defmethod parse-text 2
  [data]
  (decode-bytes (rest data) "UTF-16BE"))

(defmethod parse-text 3
  [data]
  (decode-bytes (rest data) "UTF-8"))

;;
;; Парсинг избранных тегов.
;;

(defn resolve-tag
  "Ресолвер для тегов."
  [frame]
  (:id frame))

(defmulti parse-tag #'resolve-tag)

(defmethod parse-tag "TALB"
  [frame]
  (str "Альбом: " (parse-text (:data frame))))

(defmethod parse-tag "TIT2"
  [frame]
  (str "Название трека: " (parse-text (:data frame))))

(defmethod parse-tag "TYER"
  [frame]
  (str "Год выпуска: " (parse-text (:data frame))))

(defmethod parse-tag "TCON"
  [frame]
  (str "Жанр: " (parse-text (:data frame))))

(defmethod parse-tag :default
  [frame]
  (str "Unknown tag [" (:id frame) "]: "
       ;; Текстовые теги начинаются с буквы "T". Тег "TXXX" парсится немного
       ;; иначе, пропускаем его.
       (if (and (str/starts-with? (:id frame) "T")
                (not= (:id frame) "TXXX"))
         ;; Для текстовых тегов печатаем текст.
         (parse-text (:data frame))
         ;; Для остальных список байтов.
         (str/join " " (map (partial format "%02x") (:data frame))))))

(defn main- [filename]
  (with-open [in (io/input-stream filename)]
    (let [parsed (parse-type in {:-offset 0} [] id3v2-types id3v2-format)]
      ;; Выделяем текстовые теги и показываем печатаем их значения.
      (->> (get-in parsed [:id3v2 :frames])
           (filter :valid)
           (map parse-tag)
           (str/join "\n")
           println))))

(comment
  (main- (str (System/getenv "HOME")
              "/Downloads/file-12926-ed090b.mp3")))
