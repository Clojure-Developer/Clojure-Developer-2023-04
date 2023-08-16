(ns web-scraper.core
  (:require
   [clj-http.client :as http]
   [clojure.string :as string])
  (:import
   [org.jsoup Jsoup]))


;; Scrapper protocol
(defprotocol PScraper
  (next-request [_ opts])
  (extract-data [_ page])
  (next-options [_ page opts])
  (continue? [_ opts]))


;; common flow
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


;; entry point
(->> (sequential-scraping
      (->JobsScraper "https://functional.works-hub.com")
      {:lang "clojure" :page 1})
     (flatten)
     (take 5))
