(ns web-scraper.core
  (:require
   [clj-http.client :as http]
   [clojure.string :as string])
  (:import
   [org.jsoup Jsoup]))


;; testing
(def ^String html
  (-> (http/get "https://functional.works-hub.com/jobs?interaction=1&tags=clojure")
      :body))


(def soup
  (Jsoup/parse html))


(def job-list
  (.select soup "div.jobs-board__jobs-list__content > div"))


(-> (first job-list)
    (.select "[data-test=job-salary]")
    (.text))


(-> (first job-list)
    (.select "[class^=job_card__company__name]")
    (.text))





;; first attempt
(defn get-jobs-salaries [lang]
  (let [html     ^String (-> (format "https://functional.works-hub.com/jobs?tags=%s" lang)
                             (http/get)
                             :body)
        soup     (Jsoup/parse html)
        job-list (.select soup "div.jobs-board__jobs-list__content > div")]
    (mapv (fn [job]
            (let [salary  (-> (.select job "[data-test=job-salary]")
                              (.text))
                  company (-> (.select job "[class^=job_card__company__name]")
                              (.text)
                              (string/split #",")
                              first)]
              {:company company
               :salary  salary}))
          job-list)))


(get-jobs-salaries "haskell")





;; with pagination
(defn get-job-page [lang page]
  (let [html ^String (-> (format "https://functional.works-hub.com/jobs?tags=%s&page=%s" lang page)
                         (http/get)
                         :body)]
    (Jsoup/parse html)))


(defn get-next-page [page current-page]
  (let [next-page (inc current-page)
        next?     (-> page
                      (.select (format "ul.pagination-list > li[key=%s]" next-page))
                      empty?
                      not)]
    (when next?
      next-page)))


(defn extract-salaries [page]
  (let [job-list (.select page "div.jobs-board__jobs-list__content > div")]
    (mapv (fn [job]
            (let [salary  (-> (.select job "[data-test=job-salary]")
                              (.text))
                  company (-> (.select job "[class^=job_card__company__name]")
                              (.text)
                              (string/split #",")
                              first)]
              {:company company
               :salary  salary}))
          job-list)))


(defn get-jobs-salaries [lang current-page]
  (lazy-seq
   (let [page      (get-job-page lang (or current-page 1))
         next-page (get-next-page page current-page)
         salaries  (extract-salaries page)]
     (cons salaries
           (when (some? next-page)
             (get-jobs-salaries lang next-page))))))



(->> (get-jobs-salaries "clojure" 1)
     (flatten)
     (take 5))


(->> (get-jobs-salaries "aws" 1)
     (flatten)
     (take 40))





;; generalize
(defprotocol PScraper
  (next-request [_ opts])
  (extract-data [_ page])
  (next-options [_ page opts])
  (continue? [_ opts]))


(defn sequential-scraping [scraper options]
  (lazy-seq
   (let [request      (next-request scraper options)
         html         ^String (-> request http/request :body)
         page         (Jsoup/parse html)
         page-data    (extract-data scraper page)
         next-options (next-options scraper page options)]
     (cons page-data
           (when (continue? scraper next-options)
             (sequential-scraping scraper next-options))))))



;; jobs scraper specific part
(defrecord JobsScraper [base-url]
  PScraper
  (next-request [_ {:keys [lang page]}]
    (let [url (format "%s/jobs?tags=%s&page=%s" base-url lang page)]
      {:method :get
       :url    url}))

  (extract-data [_ page]
    (let [job-list (.select page "div.jobs-board__jobs-list__content > div")]
      (mapv (fn [job]
              (let [salary  (-> (.select job "[data-test=job-salary]")
                                (.text))
                    company (-> (.select job "[class^=job_card__company__name]")
                                (.text)
                                (string/split #",")
                                first)]
                {:company company
                 :salary  salary}))
            job-list)))

  (next-options [_ page {current-page :page :as opts}]
    (let [next-page (inc current-page)
          next?     (-> page
                        (.select (format "ul.pagination-list > li[key=%s]" next-page))
                        empty?
                        not)]
      (assoc opts :page (when next? next-page))))

  (continue? [_ opts]
    (some? (:page opts))))


(->> (sequential-scraping
      (->JobsScraper "https://functional.works-hub.com")
      {:lang "clojure" :page 1})
     (flatten)
     (take 5))
