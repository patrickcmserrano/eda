(ns br.com.eda.account.db
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn insert-account!
  [ds account-data]
  (let [query (-> (h/insert-into :accounts)
                  (h/values [account-data])
                  (sql/format))]
    (jdbc/execute-one! ds query)))

(defn find-by-id
  [ds id]
  (let [query (-> (h/select :*)
                  (h/from :accounts)
                  (h/where [:= :id id])
                  (sql/format))]
    (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-lower-maps})))

(defn update-balance!
  "Atualiza o saldo de forma atômica (Incrementa ou Decrementa)."
  [ds id amount]
  (let [query (-> (h/update :accounts)
                  ;; A mágica SQL: balance = balance + amount
                  (h/set {:balance [:+ :balance amount]})
                  (h/where [:= :id id])
                  (sql/format))]
    (jdbc/execute-one! ds query)))

(defn list-all [ds]
  (let [query (-> (h/select :*)
                  (h/from :accounts)
                  (sql/format))]
    (jdbc/execute! ds query)))
