(ns br.com.eda.balance.db
  (:require [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(def schema-sql
  "
  CREATE TABLE IF NOT EXISTS balances (
      account_id VARCHAR(255) PRIMARY KEY,
      balance INT NOT NULL,
      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );
  ")

(defn migrate!
  "Cria a tabela de balances se não existir."
  [ds]
  (println ">>> [Balances] Executing Database Migrations...")
  (try
    (jdbc/execute! ds [schema-sql])
    (println ">>> [Balances] Migrations completed.")
    (catch Exception e
      (println "!!! [Balances] Migration failed:" (.getMessage e))
      (throw e))))

(defn upsert-balance!
  "Insere ou atualiza o saldo de uma conta."
  [ds account-id balance]
  (let [query (-> (h/insert-into :balances)
                  (h/values [{:account-id account-id
                              :balance    balance
                              :updated_at [:raw "CURRENT_TIMESTAMP"]}])
                  (h/on-conflict :account_id)
                  (h/do-update-set {:balance    balance
                                    :updated_at [:raw "CURRENT_TIMESTAMP"]})
                  (sql/format))]
    (jdbc/execute-one! ds query)))

(defn find-by-account-id
  [ds account-id]
  (let [query (-> (h/select :*)
                  (h/from :balances)
                  (h/where [:= :account-id account-id])
                  (sql/format))]
    (jdbc/execute-one! ds query {:builder-fn rs/as-unqualified-lower-maps})))
