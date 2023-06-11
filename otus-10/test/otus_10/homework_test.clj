(ns otus-10.homework-test
  (:require [clojure.test :refer :all]
            [otus-10.homework :as sut]))

(deftest test-parse-id3-header-flags
  (are [input expected] (= (sut/parse-id3-header-flags input) expected)
        2r00000000 {:unsynchronisation false :extended-header false :experimental-indicator false :footer-present false}
        2r00010000 {:unsynchronisation false :extended-header false :experimental-indicator false :footer-present true}
        2r00100000 {:unsynchronisation false :extended-header false :experimental-indicator true :footer-present false}
        2r00110000 {:unsynchronisation false :extended-header false :experimental-indicator true :footer-present true}
        2r01000000 {:unsynchronisation false :extended-header true :experimental-indicator false :footer-present false}
        2r01010000 {:unsynchronisation false :extended-header true :experimental-indicator false :footer-present true}
        2r01100000 {:unsynchronisation false :extended-header true :experimental-indicator true :footer-present false}
        2r01110000 {:unsynchronisation false :extended-header true :experimental-indicator true :footer-present true}
        2r10000000 {:unsynchronisation true :extended-header false :experimental-indicator false :footer-present false}
        2r10010000 {:unsynchronisation true :extended-header false :experimental-indicator false :footer-present true}
        2r10100000 {:unsynchronisation true :extended-header false :experimental-indicator true :footer-present false}
        2r10110000 {:unsynchronisation true :extended-header false :experimental-indicator true :footer-present true}
        2r11000000 {:unsynchronisation true :extended-header true :experimental-indicator false :footer-present false}
        2r11010000 {:unsynchronisation true :extended-header true :experimental-indicator false :footer-present true}
        2r11100000 {:unsynchronisation true :extended-header true :experimental-indicator true :footer-present false}
        2r11110000 {:unsynchronisation true :extended-header true :experimental-indicator true :footer-present true}))


(deftest test-sync-safe->int
  (are [input expected] (= (sut/sync-safe->int input) expected)
        [0x00 0x03 0x7f 0x7f] 0x0000ffff
        [0x25 0x37 0x27 0x2c] 0x04add3ac
        [0x00 0x00 0x00 0x00] 0x00000000
        [0x7f 0x7f 0x7f 0x7f] 0x0fffffff
        [0x00 0x00 0x01 0x10] 0x00000090))

(deftest test-parse-tag-header
  (are [input expected] (= (sut/parse-tag-header input) expected)
        [0x04 0x00 0x40 0x00 0x00 0x01 0x10] {:version 4
                                              :flags {:unsynchronisation false :extended-header true :experimental-indicator false :footer-present false}
                                              :size 144}
        [0x03 0x00 0xb0 0x25 0x37 0x27 0x2c] {:version 3
                                              :flags {:unsynchronisation true :extended-header false :experimental-indicator true :footer-present true}
                                              :size 78500780}
        [0x03 0x00] nil))

(deftest test-parse-frame-header
  (are [input expected] (= (sut/parse-frame-header input) expected)
        [0x54 0x43 0x4f 0x4e 0x00 0x00 0x00 0x0a 0x00 0x00] {:id "TCON"
                                                             :size 10
                                                             :flags [0x00 0x00]}
        [0x54 0x41 0x4c 0x42 0x00 0x00 0x00 0x16 0x00 0x01] {:id "TALB"
                                                             :size 22
                                                             :flags [0x00 0x01]}
        [0x54 0x41 0x4c 0x42] {:id "" :size 0 :flags []}))
(deftest test-read-n-bytes
  (are [input n expected] (= (let [[bytes k] (sut/read-n-bytes input n)] [(vec bytes) k]) expected)
        (java.io.ByteArrayInputStream. (byte-array 0)) 10 [[] 0]
        (java.io.ByteArrayInputStream. (byte-array [0x0a 0x11 0x22 0xb3])) 1 [[0x0a] 1]
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x01 0x02 0x03])) 2 [[0x00 0x01] 2]
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x01 0x02 0x03])) 3 [[0x00 0x01 0x02] 3]
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x01 0x02 0x03])) 4 [[0x00 0x01 0x02 0x03] 4]
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x01 0x02 0x03])) 5 [[0x00 0x01 0x02 0x03] 4]
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x01 0x02 0x03])) 5 [[0x00 0x01 0x02 0x03] 4]))

(deftest test-skip-ext-header
  (are [input expected] (= (sut/skip-ext-header input) expected)
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x00 0x00 0x08 0x01 0x02 0x03 0x04])) 4
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x00 0x00 0x0a 0x01 0x02 0x03 0x04 0x05 0x06])) 6
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x00 0x00 0x0a 0x01 0x02 0x03 0x04])) 4
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x00 0x00 0x0a 0x01])) 1
        (java.io.ByteArrayInputStream. (byte-array [0x00 0x00 0x00 0x0a])) 0))

(deftest test-skip-ext-header-if-exists
  (with-redefs-fn {#'sut/skip-ext-header (fn [_] 123)}
    #(are [input tag-header expected] (= (sut/skip-ext-header-if-exists input tag-header) expected)
          [1 2 3] {:flags {:extended-header true}} 123
          [8 8 8] {:flags {:extended-header false}} 0
          [1] {:flags {}} 0
          [1 2 3 4 5] {} 0)))

(deftest test-get-frame-header
  (are [input expected] (= (sut/get-frame-header input) expected)
        (java.io.ByteArrayInputStream. (byte-array [0x54 0x43 0x4f 0x4e 0x00 0x00 0x00 0x0a 0x00 0x00])) {:id "TCON"
                                                                                                          :size 10
                                                                                                          :flags [0x00 0x00]}
        (java.io.ByteArrayInputStream. (byte-array [0x54 0x41 0x4c 0x42 0x00 0x00 0x00 0x16 0x00 0x01])) {:id "TALB"
                                                                                                          :size 22
                                                                                                          :flags [0x00 0x01]}
        (java.io.ByteArrayInputStream. (byte-array [0x54 0x41 0x4c 0x42])) {:id "" :size 0 :flags []}))

(deftest test-decode-frame-text
  (are [input expected] (= (sut/decode-frame-text input) expected)
        [nil (int \t) (int \e) (int \s) (int \t)] ""
        [0 (int \t) (int \e) (int \s) (int \t)] "test"
        [1 0x00 0x74 0x00 0x24 0x20 0xac 0xD8 0x01 0xDC 0x37 0xD8 0x52 0xDF 0x62] "t$€\uD801\uDC37\uD852\uDF62"
        [2 0x00 0x74 0x00 0x24 0x20 0xac 0xD8 0x01 0xDC 0x37 0xD8 0x52 0xDF 0x62] "t$€\uD801\uDC37\uD852\uDF62"
        [3 (int \t) (int \e) (int \s) (int \t) 0xCE 0xA3 0xE1 0xBD 0xB2 0xD0 0xAB] "testΣὲЫ"))

(deftest test-format-frame
  (are [input expected] (= (sut/format-frame input) expected)
        {:header {:id "TALB"} :text "The Wall"} "Album: The Wall"
        {:header {:id "TIT2"} :text "Another Brick in the Wall (Part 1)"} "Name: Another Brick in the Wall (Part 1)"
        {:header {:id "TYER"} :text "1979"} "Year of release: 1979"
        {:header {:id "TCON"} :text "Art-rock"} "Genre: Art-rock"
        {:header {:id "ABCD"} :text "Test value"} "ABCD: Test value"))

(deftest test-get-frame
  (are [input expected] (= (sut/get-frame input) expected)
        (java.io.ByteArrayInputStream.
              (byte-array [0x54 0x43 0x4f 0x4e 0x00 0x00 0x00 0x0a 0x00 0x00
                           0x00 0x43 0x69 0x6e 0x65 0x6d 0x61 0x74 0x69 0x63]))
        {:header {:id "TCON" :size 10 :flags [0 0]} :text "Cinematic"}

        (java.io.ByteArrayInputStream. (byte-array [0x00 0x01]))
        {:header {:id "" :size 0 :flags []} :text ""}))

(deftest test-get-frames-seq
  (are [input total-frames-size expected] (= (sut/get-frames-seq input total-frames-size) expected)
        (java.io.ByteArrayInputStream.
              (byte-array [0x54 0x43 0x4f 0x4e 0x00 0x00 0x00 0x0a 0x00 0x00
                           0x00 0x43 0x69 0x6e 0x65 0x6d 0x61 0x74 0x69 0x63
                           0x54 0x59 0x45 0x52 0x00 0x00 0x00 0x05 0x00 0x00
                           0x00 0x32 0x30 0x31 0x34 0x01 0x01 0x00]))
        35
        '({:header {:id "TCON" :size 10 :flags [0 0]} :text "Cinematic"}
          {:header {:id "TYER" :size 5 :flags [0 0]} :text "2014"})

       (java.io.ByteArrayInputStream. (byte-array [0x54 0x43 0x4f 0x4e 0x00 0x00]))
       35
       '({:header {:flags [] :id "" :size 0} :text ""}
         {:header {:flags [] :id "" :size 0} :text ""}
         {:header {:flags [] :id "" :size 0} :text ""})

       (java.io.ByteArrayInputStream.
             (byte-array [0x54 0x43 0x4f 0x4e 0x00 0x00 0x00 0x0a 0x00 0x00
                          0x00 0x43 0x69 0x6e 0x65 0x6d 0x61 0x74 0x69 0x63]))

       9
       nil))

(deftest test-get-frames
  (with-redefs-fn {#'sut/skip-ext-header-if-exists (fn [_ _] 12)
                   #'sut/get-frames-seq (fn [_ frames-size] (cons frames-size '(1 2 3)))}
    #(are [tag-header expected] (= (sut/get-frames [] tag-header) expected)
           {:size 112} '(100 1 2 3))))

(deftest test-read-sliding-window-seq
  (are [input n k expected] (= (take k (sut/read-sliding-window-seq input n)) expected)
        (java.io.ByteArrayInputStream. (byte-array [])) 3 10 '()
        (java.io.ByteArrayInputStream. (byte-array [])) 3 0 '()

        (java.io.ByteArrayInputStream. (byte-array [0 1 2 3 4 5 6])) 3 10
        '((0 1 2) (1 2 3) (2 3 4) (3 4 5) (4 5 6))

        (java.io.ByteArrayInputStream. (byte-array [0 1 2 3 4 5 6])) 2 3
        '((0 1) (1 2) (2 3))

        (java.io.ByteArrayInputStream. (byte-array [0 1 2 3 4 5 6])) 1 10
        '((0) (1) (2) (3) (4) (5) (6))

        (java.io.ByteArrayInputStream. (byte-array [0 1 2 3 4 5 6])) 0 3
        '()))

(deftest test-is-tag-identifier?
  (are [input expected] (= (sut/is-tag-identifier? input) expected)
        [1 2 3] false
        '(1 2 3) false
        (byte-array [1 2 3]) false
        [] false
        '() false
        (byte-array []) false
        [73 68 51] true
        '(73 68 51) true
        (byte-array [73 68 51]) true))

(deftest test-get-tag-header
  (are [input expected] (= (sut/get-tag-header input) expected)
        (java.io.ByteArrayInputStream.
            (byte-array [0x49 0x44 0x33 0x04 0x00 0x40 0x00 0x00 0x01 0x10]))
        {:version 4
         :flags {:unsynchronisation false :extended-header true :experimental-indicator false :footer-present false}
         :size 144}

        (java.io.ByteArrayInputStream.
            (byte-array [0x49 0x44 0x33 0x04 0x00 0x40 0x00 0x00 0x01]))
        nil

        (java.io.ByteArrayInputStream.
            (byte-array [0x00 0x00 0x00 0x00 0x01
                         0x49 0x44 0x33 0x04 0x00 0x40 0x00 0x00 0x01]))
        nil

        (java.io.ByteArrayInputStream.
            (byte-array [0x00 0x00 0x00 0x00 0x01
                         0x49 0x44 0x33 0x04 0x00 0x40 0x00 0x00 0x01 0x10]))
        {:version 4
         :flags {:unsynchronisation false :extended-header true :experimental-indicator false :footer-present false}
         :size 144}

        (java.io.ByteArrayInputStream.
            (byte-array [0x44 0x49 0x33 0x04 0x00 0x40 0x00 0x00 0x01 0x10]))
        nil))


(deftest test-get-tag
  (let [correct-tag-bytes [0x49 0x44 0x33 0x04 0x00 0x40 0x00 0x00 0x01 0x10 0x00 0x00 0x00 0x0c 0x01 0x20
                           0x05 0x0e 0x37 0x36 0x39 0x4f 0x54 0x43 0x4f 0x4e 0x00 0x00 0x0 0x00a 0x00 0x00
                           0x00 0x43 0x69 0x6e 0x65 0x6d 0x61 0x74 0x69 0x63 0x54 0x59 0x45 0x52 0x00 0x00
                           0x00 0x05 0x00 0x00 0x00 0x32 0x30 0x31 0x34 0x54 0x44 0x52 0x43 0x00 0x00 0x00
                           0x05 0x00 0x00 0x00 0x32 0x30 0x31 0x34 0x54 0x41 0x4c 0x42 0x00 0x00 0x00 0x16
                           0x00 0x00 0x00 0x59 0x6f 0x75 0x54 0x75 0x62 0x65 0x20 0x41 0x75 0x64 0x69 0x6f
                           0x20 0x4c 0x69 0x62 0x72 0x61 0x72 0x79 0x54 0x49 0x54 0x32 0x00 0x00 0x00 0x10
                           0x00 0x00 0x0 0x049 0x6d 0x70 0x61 0x63 0x74 0x20 0x4d 0x6f 0x64 0x65 0x72 0x61
                           0x74 0x6f 0x54 0x50 0x45 0x31 0x00 0x00 0x00 0x0e 0x00 0x00 0x00 0x4b 0x65 0x76
                           0x69 0x6e 0x20 0x4d 0x61 0x63 0x4c 0x65 0x6f 0x64 0xff 0xfb 0x98 0x04 0x00 0x00]
        expected-parsed-tag {:header {:version 4
                                      :flags   {:unsynchronisation false :extended-header true :experimental-indicator false :footer-present false}
                                      :size    144}
                             :frames [{:header {:id "TCON" :flags [0 0] :size 10} :text "Cinematic"}
                                      {:header {:id "TYER" :flags [0 0] :size 5} :text "2014"}
                                      {:header {:id "TDRC" :flags [0 0] :size 5} :text "2014"}
                                      {:header {:id "TALB" :flags [0 0] :size 22} :text "YouTube Audio Library"}
                                      {:header {:id "TIT2" :flags [0 0] :size 16} :text "Impact Moderato"}
                                      {:header {:id "TPE1" :flags [0 0] :size 14} :text "Kevin MacLeod"}]}]
    (are [input expected] (= (sut/get-tag input) expected)
          (java.io.ByteArrayInputStream. (byte-array correct-tag-bytes)) expected-parsed-tag
          (java.io.ByteArrayInputStream. (byte-array (concat [0x11 0x20 0x00 0x5a 0x01] correct-tag-bytes))) expected-parsed-tag
          (java.io.ByteArrayInputStream. (byte-array (concat correct-tag-bytes [0x11 0x00 0xaa]))) expected-parsed-tag
          (java.io.ByteArrayInputStream. (byte-array (assoc-in correct-tag-bytes [0] 0x0a))) nil
          (java.io.ByteArrayInputStream. (byte-array (assoc-in correct-tag-bytes [1] 0x0a))) nil
          (java.io.ByteArrayInputStream. (byte-array (assoc-in correct-tag-bytes [2] 0x0a))) nil
          (java.io.ByteArrayInputStream. (byte-array (concat [0x00 0x01] (assoc-in correct-tag-bytes [2] 0x0a)))) nil
          (java.io.ByteArrayInputStream. (byte-array 0)) nil
          (java.io.ByteArrayInputStream. (byte-array 100)) nil)))

