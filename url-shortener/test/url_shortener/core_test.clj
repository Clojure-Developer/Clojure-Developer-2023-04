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
   (are [expected actual] (= expected actual)
     "otus" (sut/id->url 3410886)
     "test" (sut/id->url 4544743)
     "007" (sut/id->url 203171)))

 (testing "id->url->id property convertion"
   (tc/quick-check 10
     (prop/for-all [id gen/nat]
                   (is (= id (sut/url->id (sut/id->url id)))))))

 (testing "throws exception on invalid arguments"
   (is (thrown? Exception (sut/id->url "otus")))))

(deftest url->id-test
  (testing "returns valid number"
    (are [expected actual] (= expected actual)
      3410886 (sut/url->id "otus")
      4544743 (sut/url->id "test")
      203171 (sut/url->id "007")))

  (testing "url->id->url property convertion"
    (let [strings-from-symbols (gen/fmap str/join
                                (gen/vector (gen/elements sut/symbols)))]
      (tc/quick-check 10
       (prop/for-all [url strings-from-symbols]
                     (is (= url (sut/id->url (sut/url->id url))))))))

  (testing "throws exception on invalid arguments"
    (is (thrown? Exception (sut/url->id "http://otus-url/")))
    (is (thrown? Exception (sut/url->id 99999)))))

(use-fixtures :each
  tu/fix-with-url-db)

(deftest ^:integration url-shortener-test
  (testing "should be able to shorten a url"
    (let [url "https://www.google.com/"
          expected "Your short URL: http://otus-url/b"
          shorten-output (tu/get-output (sut/shorten-url url))]
        (is (= expected shorten-output))))

  (testing "should be able to shorten a url and get it back"
    (let [url "https://www.google.com/"
          shorter-output (tu/get-output (sut/shorten-url url))
          shorter-url (str/replace shorter-output #"Your short URL: " "")
          find-long-url-output (tu/get-output (sut/find-long-url shorter-url))]
      (is (= find-long-url-output (str "Your original URL: " url))))))
