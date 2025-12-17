(ns br.com.eda.client.interface
  (:require [br.com.eda.client.core :as core]))

(defn create-client!
  "Cria um novo cliente.
   Params:
     ds    - Datasource do banco (injetado)
     input - Mapa {:name \"...\" :email \"...\"}"
  [ds input]
  (core/create-client! ds input))