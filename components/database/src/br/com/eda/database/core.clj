(ns br.com.eda.database.core
  (:require [next.jdbc :as jdbc])
  (:import [com.zaxxer.hikari HikariDataSource]))

;; --- Configuração do Pool de Conexões ---

(defn- db-spec [config]
  {:dbtype "postgresql"
   :dbname (or (:dbname config) "wallet")
   :host   (or (:host config) "localhost")
   :port   (or (:port config) 5432)
   :user   (or (:user config) "root")
   :password (or (:password config) "root")})

(defn get-datasource
  "Cria e retorna um DataSource (Pool de conexões) otimizado."
  ([] (get-datasource {}))
  ([config]
   (let [spec (db-spec config)
         ds   (HikariDataSource.)]
     (doto ds
       (.setJdbcUrl (str "jdbc:postgresql://" (:host spec) ":" (:port spec) "/" (:dbname spec)))
       (.setUsername (:user spec))
       (.setPassword (:password spec))
       (.setMaximumPoolSize 10)
       (.setMinimumIdle 2)
       (.setPoolName "WalletPool"))
     ;; Retorna o objeto DataSource que o next.jdbc consome nativamente
     ds)))

;; --- Migrations (DDL) ---

(def schema-sql
  "
  CREATE TABLE IF NOT EXISTS clients (
      id VARCHAR(255) PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      email VARCHAR(255) NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  );

  CREATE TABLE IF NOT EXISTS accounts (
      id VARCHAR(255) PRIMARY KEY,
      client_id VARCHAR(255) NOT NULL,
      balance INT NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      CONSTRAINT fk_client FOREIGN KEY (client_id) REFERENCES clients(id)
  );

  CREATE TABLE IF NOT EXISTS transactions (
      id VARCHAR(255) PRIMARY KEY,
      account_id_from VARCHAR(255) NOT NULL,
      account_id_to VARCHAR(255) NOT NULL,
      amount INT NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      CONSTRAINT fk_acc_from FOREIGN KEY (account_id_from) REFERENCES accounts(id),
      CONSTRAINT fk_acc_to FOREIGN KEY (account_id_to) REFERENCES accounts(id)
  );
  ")

(defn migrate!
  "Executa a criação das tabelas no banco de dados."
  [datasource]
  (println ">>> Executing Database Migrations...")
  (try
    (jdbc/execute! datasource [schema-sql])
    (println ">>> Migrations completed successfully.")
    (catch Exception e
      (println "!!! Migration failed:" (.getMessage e))
      (throw e))))