(ns br.com.eda.client.db
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn insert-client!
  "Insere um cliente no banco de dados."
  [ds client-data]
  (let [query (-> (h/insert-into :clients)
                  (h/values [client-data])
                  (h/returning :*)
                  (sql/format))]
    (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-kebab-maps})))

(defn list-all [ds]
  (let [query (-> (h/select :*)
                  (h/from :clients)
                  (sql/format))]
    (jdbc/execute! ds query)))