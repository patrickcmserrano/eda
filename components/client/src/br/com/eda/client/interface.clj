(ns br.com.eda.client.interface
  (:require [br.com.eda.client.core :as core]
            [br.com.eda.client.db :as db]))

(defn create-client!
  "Cria um novo cliente.
   Params:
     ds    - Datasource do banco (injetado)
     input - Mapa {:name \"...\" :email \"...\"}"
  [ds input]
  (core/create-client! ds input))

(defn list-all [ds]
  (db/list-all ds))