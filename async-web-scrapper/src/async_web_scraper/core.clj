(ns async-web-scraper.core
  (:require
   [clj-http.client :as http]
   [clojure.core.async
    :as async
    :refer [<! <!! >! chan close! go pipeline pipeline-async thread]]
   [clojure.string :as string])
  (:import
   (org.jsoup Jsoup)))


(defn async-request [opts]
  (thread (http/request opts)))


(defprotocol PAsyncScraper
  (request [_ href])
  (extract-tags-hrefs [_ page])
  (extract-pagination-hrefs [_ page])
  (extract-data [_ page]))


(defrecord JobsScraper [base-url entrypoint]
  PAsyncScraper
  (request [_ href]
    (go (-> (<! (async-request {:url    (str base-url href)
                                :method :get}))
            :body
            (Jsoup/parse))))

  (extract-tags-hrefs [_ page]
    (-> page
        (.select "div[class^=job_card__card] ul[class*=tags] > a.tag")
        (->> (map #(.attr % "href")))))

  (extract-pagination-hrefs [_ page]
    (or (seq (-> page
                 (.select "ul.pagination-list a")
                 (->> (map #(.attr % "href")))))
        [""]))

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
            job-list))))


(defn async-scraping [scraper & {:keys [limit-branching]}]
  (let [tags-c        (chan)
        paginations-c (chan 8)
        data-c        (chan 16)
        out-c         (chan 16)]

    (pipeline-async 1 paginations-c (fn [href result]
                                      (go
                                        (let [page (<! (request scraper href))]
                                          (doseq [pagination-href (extract-pagination-hrefs scraper page)]
                                            (>! result (str href pagination-href)))
                                          (close! result)))) tags-c)

    (pipeline-async 1 data-c (fn [href result]
                               (go (>! result (<! (request scraper href)))
                                   (close! result))) paginations-c)

    (pipeline 4 out-c (map (partial extract-data scraper)) data-c)

    (go (let [entry-page (<! (request scraper (:entrypoint scraper)))]
          (doseq [href (cond->> (extract-tags-hrefs scraper entry-page)
                                limit-branching (take limit-branching))]
            (>! tags-c href))
          (close! tags-c)))

    out-c))


(comment
 (def scraper (->JobsScraper "https://functional.works-hub.com" "/jobs"))
 (def ch (async-scraping scraper :limit-branching 5))

 (<!! ch)
 (count (flatten (<!! (async/into [] ch))))

 (flatten (<!! (async/take 5 ch))))
