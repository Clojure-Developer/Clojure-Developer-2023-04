(ns otus-18.homework.pokemons
  (:require [clj-http.client :as http]
            [clojure.core.async :as async :refer [<! <!! >!! >! chan go]]
            [cheshire.core :as json]))
   
(def base-url     "https://pokeapi.co/api/v2")
(def pokemons-url (str base-url "/pokemon/"))
(def types-url    (str base-url "/type/"))

(defn async-get
  "Заворачиваем запрос в тред."
  [url]
  (async/thread
    (-> url http/get :body (json/parse-string true))))

(defn c>get-entities-list
  "Берет из канала url и загружает список сущностей. Полученный
  список отправляется в выходной канал.
  Если это не последняя страница, то url следующей страницы отправляется
  во входной канал."
  [c-pages-urls page-url c-entities-urls]
  (go
    (let [response (<! (async-get page-url))
          next-url (:next response)]
      ;; Отправляем url'ы страниц с информацией о сущностях в выходной канал.
      (doseq [entity (:results response)]
        (>! c-entities-urls (:url entity)))
      (if next-url
        ;; Отправляем url следующей страницы во входной канал.
        (>! c-pages-urls next-url)
        ;; Или закрываем канал, если следующей страницы нет. Если его не закрыть, 
        ;; пайплайн не закроет автоматически выходной канал.
        (async/close! c-pages-urls))
      (async/close! c-entities-urls))))


(defn c>get-entities-data
  "Получает информацию о сущностях по url'у, содержащемуся во входном
  значении и отправляет ее в выходной канал, который и возвращает."
  [entity-url c-entities-data]
  (go
    (let [response (<! (async-get entity-url))]
      (>! c-entities-data response)
      (async/close! c-entities-data))))

(defn get-entities-data
  "Получает информацию по заданным сущностям. На вход передается ссылка на первую страницу
  со списком требуемых сущностей и функция для парсинга (выделения нужных данных из описания
  сущности).
  Возвращает канал с распаршенными данными."
  [initial-page-url parse-fn]
  (let [;; Это канал для url'ов, ссылающихся на страницы со списоком сущностей.
        ;; Минимальная длина буфера - 2 из-за того, что мы отправляем
        ;; во входной поток новые url'ы страниц со списком сущностей.
        ;; Если сделать длину буфера 1, канал заблокируется при отправке
        ;; в него нового url.
        c-pages-urls      (chan 2)
        ;; Это канал для url'ов, ссылающихся на описания сущностей.
        c-entities-urls   (chan 10)
        ;; Это канал для данных по сущностям.
        c-entities-data   (chan 4)
        ;; Это канал для распаршенных данных - выходной канал.
        c-entities-parsed (chan 4)]
    ;; В этом пайплайне берем страницы со списком сущностей и возвращаем ссылки на описания сущностей.
    ;; Трансформирующая функция замыкает входной канал, т.к. она будет добавлять в этот канал ссылку
    ;; на следующую страницу.
    (async/pipeline-async 1 c-entities-urls (partial c>get-entities-list c-pages-urls) c-pages-urls)
    ;; В этом пайплайне берем ссылки на описания сущностей и возвращаем данные для каждой сущности.
    (async/pipeline-async 4 c-entities-data c>get-entities-data c-entities-urls)
    ;; В этом пайплайне берем описания сущностей и возвращаем только нужные нам данные
    ;; (за это отвечает переданная в функцию функция parse-fn).
    (async/pipeline 4 c-entities-parsed (map parse-fn) c-entities-data)
    ;; Закидываем во входной канал url первой страницы со списком сущностей.
    (>!! c-pages-urls initial-page-url)
    ;; Возвращаем выходной канал.
    c-entities-parsed))

(defn parse-type-names
  "На вход принимает ответ API с описанием типов покемонов. Из ответа
  выделяет названия типов на различных языках, возвращает мапу."
  [response]
  [(:name response)
   (into {} (map (fn [i18n-name]
                   [(get-in i18n-name [:language :name])
                    (get-in i18n-name [:name])])
                 (:names response)))])

(defn parse-pokemon-types
  "На вход принимает ответ API с описанием покемона. Из ответа выделяет
  список типов покемонов."
  [response] [(:name response)
              (mapv #(get-in % [:type :name])
                    (:types response))])

(defn get-pokemons
  "Асинхронно запрашивает список покемонов и название типов в заданном языке.
  Возвращает map, где ключами являются имена покемонов (на английском английский),
  а значения - коллекция названий типов на заданном языке."
  [& {:keys [limit lang] :or {limit 50 lang "ja"}}]
  (let [;; Получаем названия типов на разных языках.
        pokemon-types (->> (get-entities-data types-url parse-type-names)
                           (async/into {})
                           <!!)]
    (->> ;; Получаем список покемонов.
         (async/take limit (get-entities-data pokemons-url parse-pokemon-types))
         (async/into [])
         <!!
         ;; Для каждого покемона получаем названия типов на заданном языке.
         (map (fn [[name types]]
                [name (mapv (fn [t]
                              (get-in pokemon-types [t lang]))
                            types)]))
         (into {}))))

(comment
  (<!! (async/into {} (async/take 2 (get-entities-data types-url parse-type-names))))
  (<!! (async/into {} (async/take 2 (get-entities-data pokemons-url parse-pokemon-types))))
  (get-pokemons :limit 10)
  (get-pokemons :limit 5 :lang "en"))
