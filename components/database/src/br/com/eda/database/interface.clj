(ns br.com.eda.database.interface
  (:require [br.com.eda.database.core :as core]))

(defn get-datasource
  "Retorna um javax.sql.DataSource configurado (HikariCP).
   Aceita um mapa opcional de config: {:host ... :user ... :password ...}"
  ([] (core/get-datasource))
  ([config] (core/get-datasource config)))

(defn migrate!
  "Cria as tabelas necessárias no banco de dados."
  [datasource]
  (core/migrate! datasource))