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