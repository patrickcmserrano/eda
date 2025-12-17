(ns br.com.eda.client.core
  (:require [br.com.eda.client.db :as db]
            [malli.core :as m]
            [malli.error :as me])
  (:import [java.util UUID]))

;; --- Schema ---
(def ClientSchema
  [:map
   [:id :string]
   [:name [:string {:min 3}]]
   [:email [:string {:min 5}]]
   ;; createdAt é opcional no input pois o banco gera, 
   ;; mas bom ter no schema se formos ler.
   [:created_at {:optional true} inst?]])

(def CreateClientInput
  [:map
   [:name [:string {:min 3}]]
   [:email [:string {:min 5}]]])

;; --- Lógica ---

(defn create-client!
  [ds input]
  ;; 1. Validação
  (if-not (m/validate CreateClientInput input)
    (throw (ex-info "Dados inválidos" {:errors (me/humanize (m/explain CreateClientInput input))}))

    ;; 2. Preparação (Gerar ID)
    (let [new-client (assoc input :id (str (UUID/randomUUID)))]

      ;; 3. Persistência
      (db/insert-client! ds new-client)

      ;; 4. Retorno
      new-client)))