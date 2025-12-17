(ns br.com.eda.client.db
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [next.jdbc :as jdbc]))

(defn insert-client!
  "Insere um cliente no banco de dados."
  [ds client-data]
  (let [query (-> (h/insert-into :clients)
                  (h/values [client-data])
                  (sql/format))]
    (jdbc/execute-one! ds query)))