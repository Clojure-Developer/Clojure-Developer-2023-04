(ns otus-21.homework.core-test
  (:require [clojure.test :refer :all]
            [otus-21.homework.core :refer :all]))

(def example
  "$ cd /
$ ls
dir a
14848514 b.txt
8504156 c.dat
dir d
$ cd a
$ ls
dir e
29116 f
2557 g
62596 h.lst
$ cd e
$ ls
584 i
$ cd ..
$ cd ..
$ cd d
$ ls
4060174 j
8033020 d.log
5626152 d.ext
7214296 k
")

(deftest sum-of-sizes-test
  (testing "Work with the example"
    (is (= (sum-of-sizes example)
           95437))))
