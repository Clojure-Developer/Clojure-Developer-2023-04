(ns url-shortener.core-test
  (:require [clojure.test :refer [is are testing deftest use-fixtures]]
            [clojure.string :as str]
            [url-shortener.core :as sut]
            [url-shortener.test-util :as tu]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))

(deftest id->url-test
 (testing "returns valid string"
   (are [expected actual] (= expected (sut/id->url actual))
     "otus" 3410886
     "test" 4544743
     "007" 203171))

 (testing "id->url->id property convertion"
   (tc/quick-check 10
     (prop/for-all [id gen/nat]
                   (is (= id (sut/url->id (sut/id->url id)))))))

 (testing "throws exception on invalid arguments"
   (is (thrown? Exception (sut/id->url "otus")))))

(deftest url->id-test
  (testing "returns valid number"
    (are [expected actual] (= expected (sut/url->id actual))
      3410886 "otus"
      4544743 "test"
      203171 "007"))

  (testing "url->id->url property convertion"
    (let [strings-from-symbols (gen/fmap str/join
                                (gen/vector (gen/elements sut/symbols)))]
      (tc/quick-check 10
       (prop/for-all [url strings-from-symbols]
                     (is (= url (sut/id->url (sut/url->id url))))))))

  (testing "throws exception on invalid arguments"
    (are [input] (thrown? Exception (sut/url->id input))
      "http://otus-url/"
      123123)))

(use-fixtures :each
  tu/fix-with-url-db)

(deftest ^:integration url-shortener-test
  (testing "should be able to shorten a url"
    (let [url "https://www.google.com/"
          expected "Your short URL: http://otus-url/b\n"
          shorten-output (with-out-str (sut/shorten-url url))]
        (is (= expected shorten-output))))

  (testing "should be able to shorten a url and get it back"
    (let [url "https://www.google.com/"
          shorter-output (with-out-str (sut/shorten-url url))
          shorter-url (str/trim (str/replace shorter-output #"Your short URL: " ""))
          find-long-url-output (with-out-str (sut/find-long-url shorter-url))]
      (is (= find-long-url-output (str "Your original URL: " url "\n"))))))
