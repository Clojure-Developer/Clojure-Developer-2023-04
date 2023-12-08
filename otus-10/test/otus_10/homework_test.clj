(ns otus-10.homework-test
  (:require [clojure.test :refer :all]
            [otus-10.homework :as sut]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(deftest test-get-bytes
  (testing "Get bytes"
    (with-open [in (io/input-stream (.getBytes "0123456789ABCDEF"))]
      (is (= (sut/get-bytes in 4) (mapv int "0123")))
      (is (= (sut/get-bytes in 2) (mapv int "45")))
      (is (= (sut/get-byte in) 16r36)))
    (let [values [16rFF 16r80 16r40 16r00]]
      (with-open [in (io/input-stream (byte-array 4 values))]
        (is (= (sut/get-bytes in 4) values))))))

(deftest test-parse-type
  (testing "Parse type"
    (let [parse (fn [input fmt]
                  (with-open [in (io/input-stream (if (= (type input) java.lang.String)
                                                    (.getBytes input)
                                                    (byte-array (count input) input)))]
                    (sut/parse-type in {:-offset 0} [] {} fmt)))]

      (is (= {:-offset 1 :b 16r30}
             (parse "0" {:name :b :type :u1})))

      (is (= {:-offset 2 :m "12"}
             (parse "12" {:name :m :type :magic :contents "12"})))

      (is (thrown? Exception (parse "XX" {:name :m :type :magic :contents "12"})))

      (is (thrown? Exception (parse "XX" {:name :m :type :magic})))

      (is (= {:-offset 1 :f {:f7 true :f6 false :f5 true :f4 false :f3 false :f2 true :f1 false :f0 true}}
             (parse [2r10100101] {:name :f :type :flags :flags [:f7 :f6 :f5 :f4 :f3 :f2 :f1 :f0]})))

      (is (= {:-offset 1 :f {:b1 1 :b2 1 :b3 5 :b4 0}}
             (parse [2r10101010]
                    {:name :f :type :bit-values 
                     :values [[:b1 1] [:b2 2] [:b3 4] [:b4 1]]})))

      (is (= {:-offset 4 :s 16r01010101}
             (parse (str/join (map char [8 4 2 1]))
                    {:name :s :type :size})))

      (is (= {:-offset 4 :d [16r41 16r41 16r41 16r41]}
             (parse "AAAA"
                    {:name :d :type :data :size-fn (constantly 4)})))

      (is (= {:-offset 4 :s "TEST"}
             (parse "TEST"
                    {:name :s :type :str :length 4})))

      (is (= {:-offset 0 :i "XXX"}
             (parse "TEST" {:name :i :type :instance :value-fn (constantly "XXX")})))

      (is (= {:-offset 4 :u1 16r41 :u2 16r61 :s0 "Aa"}
             (parse "AaAa" {:type :seq :seq [{:name :u1 :type :u1}
                                             {:name :u2 :type :u1}
                                             {:name :s0 :type :str :length 2}]})))

      (is (thrown? Exception (parse "XXXX" {:name :s :type :seq})))

      (is (= {:-offset 12 :r ["AAA" "BBB" "CCC" "END"]}
             (parse "AAABBBCCCENDXXX"
                    {:name :r
                     :type :repeat
                     :repeat {:type :str :length 3}
                     :repeat-while-fn (fn [parsed _path]
                                        (not= (last (:r parsed)) "END"))})))

      (is (thrown? Exception (parse "XXX" {:name :n :type :unknown}))))))


(deftest test-decode-bytes
  (testing "Bytes decoding"
    (are [string args] (= string (apply sut/decode-bytes args))
         "Proverka" [[16r50 16r72 16r6f 16r76 16r65 16r72 16r6b 16r61]
                     "ISO-8859-1"]
         "Проверка" [[16rfe 16rff 16r04 16r1f 16r04 16r40 16r04 16r3e
                      16r04 16r32 16r04 16r35 16r04 16r40 16r04 16r3a
                      16r04 16r30]
                     "UTF-16"]
         "Проверка" [[16r04 16r1f 16r04 16r40 16r04 16r3e 16r04 16r32
                      16r04 16r35 16r04 16r40 16r04 16r3a 16r04 16r30]
                     "UTF-16BE"]
         "Проверка" [[16rd0 16r9f 16rd1 16r80 16rd0 16rbe 16rd0 16rb2
                      16rd0 16rb5 16rd1 16r80 16rd0 16rba 16rd0 16rb0]
                     "UTF-8"])))

(deftest test-parse-text
  (testing "Text tag decoding"
    (are [string bs] (= string (sut/parse-text bs))
         "Proverka" [0 16r50 16r72 16r6f 16r76 16r65 16r72 16r6b 16r61]
         "Проверка" [1 16rfe 16rff 16r04 16r1f 16r04 16r40 16r04 16r3e
                     16r04 16r32 16r04 16r35 16r04 16r40 16r04 16r3a
                     16r04 16r30]
         "Проверка" [2 16r04 16r1f 16r04 16r40 16r04 16r3e 16r04 16r32
                     16r04 16r35 16r04 16r40 16r04 16r3a 16r04 16r30]
         "Проверка" [3 16rd0 16r9f 16rd1 16r80 16rd0 16rbe 16rd0 16rb2
                     16rd0 16rb5 16rd1 16r80 16rd0 16rba 16rd0 16rb0])))

(deftest test-parse-tag
  (testing "Tag decoding"
    (are [tag frame] (= tag (sut/parse-tag frame))
         "Альбом: Album"
         {:id "TALB" :data (cons 3 (mapv int "Album"))}

         "Название трека: Track"
         {:id "TIT2" :data (cons 3 (mapv int "Track"))}

         "Год выпуска: 2032"
         {:id "TYER" :data (cons 3 (mapv int "2032"))}

         "Жанр: Genre"
         {:id "TCON" :data (cons 3 (mapv int "Genre"))}

         "Unknown tag [TABC]: Unknown"
         {:id "TABC" :data (cons 3 (mapv int "Unknown"))}

         (str "Unknown tag [BABC]: " (str/join " " (map #(format "%02x" (int %)) "Unknown")))
         {:id "BABC" :data (mapv int "Unknown")})))

