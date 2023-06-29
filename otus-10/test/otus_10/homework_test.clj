(ns otus-10.homework-test
  (:require [clojure.test :refer :all]
            [otus-10.homework :as sut]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(deftest test-get-bytes
  (with-open [in (io/input-stream (.getBytes "0123456789ABCDEF"))]
    (is (= (sut/get-bytes in 4) (mapv int "0123")))
    (is (= (sut/get-bytes in 2) (mapv int "45")))
    (is (= (sut/get-byte in) 16r36)))
  (let [values [16rFF 16r80 16r40 16r00]]
    (with-open [in (io/input-stream (byte-array 4 values))]
      (is (= (sut/get-bytes in 4) values)))))

(deftest test-parse-type
  (let [parse (fn [input fmt]
                (with-open [in (io/input-stream (if (= (type input) java.lang.String)
                                                  (.getBytes input)
                                                  (byte-array (count input) input)))]
                  (sut/parse-type in {:-offset 0} [] {} fmt)))]

      (is (= (parse "0" {:name :b :type :u1})
             {:-offset 1 :b 16r30}))

      (is (= (parse "12" {:name :m :type :magic :contents "12"})
             {:-offset 2 :m "12"}))

      (is (thrown? Exception
                   (parse "XX" {:name :m :type :magic :contents "12"})))

      (is (thrown? Exception
                   (parse "XX" {:name :m :type :magic})))

      (is (= (parse [2r10100101]
                    {:name :f :type :flags :flags [:f7 :f6 :f5 :f4 :f3 :f2 :f1 :f0]})
             {:-offset 1 :f {:f7 true :f6 false :f5 true :f4 false :f3 false :f2 true :f1 false :f0 true}}))

      (is (= (parse [2r10101010]
                    {:name :f :type :bit-values 
                     :values [[:b1 1] [:b2 2] [:b3 4] [:b4 1]]})
             {:-offset 1 :f {:b1 1 :b2 1 :b3 5 :b4 0}}))

      (is (= (parse (str/join (map char [8 4 2 1]))
                    {:name :s :type :size})
             {:-offset 4 :s 16r01010101}))

      (is (= (parse "AAAA"
                    {:name :d :type :data :size-fn (constantly 4)})
             {:-offset 4 :d [16r41 16r41 16r41 16r41]}))

      (is (= (parse "TEST"
                    {:name :s :type :str :length 4})
             {:-offset 4 :s "TEST"}))

      (is (= (parse "TEST" {:name :i :type :instance :value-fn (constantly "XXX")})
             {:-offset 0 :i "XXX"}))

      (is (= (parse "AaAa" {:type :seq :seq [{:name :u1 :type :u1}
                                             {:name :u2 :type :u1}
                                             {:name :s0 :type :str :length 2}]})
             {:-offset 4 :u1 16r41 :u2 16r61 :s0 "Aa"}))

      (is (thrown? Exception
                   (parse "XXXX" {:name :s :type :seq})))
      ))
