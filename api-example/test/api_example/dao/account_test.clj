(ns api-example.dao.account-test
  (:require [clojure.test :refer :all]
            [api-example.core :refer [*ctx*]]
            [api-example.dao.account :as sut]
            [api-example.test-util :as tu]
            [api-example.util :as u]
            [matcher-combinators.test]))

(use-fixtures :each
  tu/fix-with-reference-db)

(deftest test-get-all
  (let [{:keys [ds]} *ctx*

        expected-matcher
        [{:id 1
          :username "admin"
          :password "password123"
          :email "admin@otus.ru"
          :created-on (partial instance? java.util.Date)}
         {:id 2
          :username "nikita"
          :password "password"
          :email "nikita@otus.ru"
          :created-on (partial instance? java.util.Date)}]]

    (is (match? expected-matcher (sut/get-all ds)))))

(deftest test-get-by-id
  (let [{:keys [ds]} *ctx*]

    (testing "Entity is found."
      (let [expected-matcher {:id 1
                              :username "admin"
                              :password "password123"
                              :email "admin@otus.ru"
                              :created-on (partial instance? java.util.Date)}]
        (is (match? expected-matcher (sut/get-by-id ds 1)))))

    (testing "Entity is not found."
      (is (nil? (sut/get-by-id ds 3))))))

(deftest test-create
  (let [{:keys [ds]} *ctx*
        entity {:username "test"
                :password "test"
                :email "test@test.com"
                :created-on (u/now)}]

    (testing "Entity is correct and ..."
      (testing "... not exists yet."
        (is (match? (assoc entity :id int?) (sut/create ds entity))))

      (testing "... already exists."
        (is (thrown-with-msg? org.postgresql.util.PSQLException
                              #"duplicate key value violates unique constraint"
                              (sut/create ds (assoc entity :username "admin"))))))

    (testing "Entity is not correct."
      (testing "Missing field."
        (is (thrown-with-msg? org.postgresql.util.PSQLException
                              #"null value in column"
                              (sut/create ds (dissoc entity :email)))))

      (testing "Extra field."
        (is (thrown-with-msg? org.postgresql.util.PSQLException
                              #"does not exist"
                              (sut/create ds (assoc entity :extra "value"))))))))
