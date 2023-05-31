(ns api-example.api.account-test
  (:require [clojure.test :refer :all]
            [api-example.core :refer [*ctx*]]
            [api-example.api.account :as sut]
            [api-example.test-util :as tu]
            [api-example.util :as u]
            [ring.mock.request :as mock]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]))

(use-fixtures :each
  tu/fix-with-reference-db)

(use-fixtures :once
  tu/fix-with-now)

(deftest ^:integration test-routes
  (let [entity-to-create {:username "test"
                          :password "test"
                          :email "test@test.com"}
        created-entity (assoc entity-to-create
                              :id int?
                              :created-on (u/now))]

    (testing "Create."
      (let [actual (-> (mock/request :post "/accounts")
                       (assoc :body entity-to-create)
                       (assoc :ctx *ctx*)
                       (sut/routes))]
        (is (match? {:status 200 :body created-entity} actual))))

    (testing "Get all."
      (let [actual (-> (mock/request :get "/accounts")
                       (assoc :ctx *ctx*)
                       (sut/routes))
            entities [{:id int?
                       :username "admin"
                       :password "password123"
                       :email "admin@otus.ru"
                       :created-on (partial instance? java.util.Date)}
                      {:id int?
                       :username "nikita"
                       :password "password"
                       :email "nikita@otus.ru"
                       :created-on (partial instance? java.util.Date)}
                      created-entity]]
        (is (match? {:status 200 :body entities} actual))))))

(comment
  (require '[matcher-combinators.matchers :as m])

  (m/matcher-for (m/equals {:a 1
                            :b (m/embeds {:c 1
                                          :d 2})}))

  (is (match? (m/equals {:a 1
                         :b (m/equals {:c 2})})
              {:a 1
               :b {:c 2
                   :d 3}}))

  (is (match? (m/equals {:a 1
                         :b {:c 2}})
              {:a 1
               :b {:c 2
                   :d 3}}))
  )
