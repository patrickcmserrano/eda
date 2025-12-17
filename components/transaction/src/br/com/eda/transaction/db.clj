(ns br.com.eda.transaction.db
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [next.jdbc :as jdbc]))

(defn insert-transaction!
  "Salva a transação. O HoneySQL converte kebab-case (Clojure) para snake_case (SQL)."
  [tx transaction-data]
  (let [query (-> (h/insert-into :transactions)
                  (h/values [transaction-data])
                  (sql/format))]
    (jdbc/execute-one! tx query)))

(defn get-statement
  "Busca transações onde a conta participou (seja enviando ou recebendo)."
  [ds account-id]
  (let [query (-> (h/select :*)
                  (h/from :transactions)
                  (h/where [:or
                            [:= :account_id_from account-id]
                            [:= :account_id_to account-id]])
                  (h/order-by [:created_at :desc]) ;; Mais recentes primeiro
                  (sql/format))]
    (jdbc/execute! ds query)))