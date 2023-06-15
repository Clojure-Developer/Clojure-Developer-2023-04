(ns id3-tag.core-test
  (:require [clojure.test :refer :all]
            [id3-tag.core :as sut]))

(deftest rarse-test
      (testing "file1.mp3"
        (is (= {:title "Impact Moderato" 
                :ganre "Cinematic"
                :year 2014, 
                :album "YouTube Audio Library"}
               (sut/parse-id3 "file1.mp3"))))) 