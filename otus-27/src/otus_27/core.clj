(ns otus-27.core
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as jsql]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

;; * Data Source

(def db (jdbc/get-datasource {:dbtype "h2:mem"
                              :dbname "example"}))

;; * Primitives

;; ** SQL execution

(comment
  (let [conn (jdbc/get-connection db)
        res
        (jdbc/execute!
         conn
         ["select * from information_schema.tables"])]
    (.close conn)
    res)

  (jdbc/execute-one! db ["
select id from information_schema.tables
  where table_name = $1
" "USERS"])

  )

;; ** ResultSet

(comment
  (jdbc/execute! db ["select * from information_schema.tables limit 5"]
                 {:builder-fn rs/as-arrays})

  (jdbc/execute! db ["select * from information_schema.tables limit 5"]
                 {:builder-fn rs/as-unqualified-kebab-maps})

  )

;; ** plan

(comment
  (run!
   #(println (:id %))
   (jdbc/plan db ["select * from information_schema.tables limit 5"]
              {:builder-fn rs/as-unqualified-kebab-maps}))

  )

;; * Practice

(comment
  (jdbc/execute-one! db ["
create table owners (
  id int auto_increment primary key,
  name varchar(255) not null
)
"])

  (jdbc/execute-one! db ["
create table pets (
  id int auto_increment primary key,
  name varchar(255) not null,
  owner int not null
    references owners(id)
    on delete cascade
)
"])

  (jdbc/execute!
   db
   ["insert into owners (name)
      values (?), (?)"

    "Bob"
    "Ann"])

  (jsql/insert! db :owners {:name "Tom"})

  (jdbc/execute! db ["select id, name from owners"])

  (jdbc/with-transaction [tx db]
    (jsql/insert-multi!
     tx :pets
     [{:name "Skipper" :owner 1}
      {:name "Spot" :owner 2}
      {:name "Stinky" :owner 2}
      {:name "Jerry" :owner 3}]))

  (jsql/find-by-keys db :pets {:owner 2})

  (jdbc/execute!
   db ["
select p.name as pet, o.name as owner from pets as p
left join owners as o
on p.owner = o.id
"])

  )

;; * HoneySQL

(comment
  (jdbc/execute!
   db
   (sql/format {:from [[:pets :p]]
                :select [:p.name :o.name]
                :where [[:= :name [:param :a]]]
                :left-join [[:owners :o]
                            [:= :p.owner :o.id]]}
               {:params {:a "asd"}}))

  (jdbc/execute!
   db
   (sql/format '{from ((pets p))
                 select (p.name, o.name)
                 where ((= p.name (param :a)))
                 left-join ((owners o)
                            (= p.owner o.id))}
               {:params {:a "foo"}}))

  (jdbc/execute!
   db
   (sql/format (-> (h/select :p.name :p.name)
                   (h/from [:pets :p])
                   (h/where [:= :p.name [:param :a]])
                   (h/left-join [:owners :o]
                                [:= :p.owner :o.id]))
               {:params {:a "foo"}}))

  )
