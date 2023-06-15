(ns id3-tag.tag-test
  (:require [clojure.test :refer :all]
            [id3-tag.tag :as sut]))

(deftest parse-tag-test
  (testing "parse-tag"
    (are [ans tag] (= ans (sut/parse-tag tag))
      {:album "Album"} ["TALB" "Album"]
      {:title "Title"} ["TIT2" "Title"]
      {:ganre "Ganre"} ["TCON" "Ganre"]
      {:year 2004} ["TYER" "2004"]
      {:year nil} ["TYER" "abc"]
      nil ["ANY" "TEST"]))) 
