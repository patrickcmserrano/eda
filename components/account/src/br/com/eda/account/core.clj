(ns br.com.eda.account.core
  (:require [br.com.eda.account.db :as db]
            [malli.core :as m]
            [malli.error :as me])
  (:import [java.util UUID]))

;; --- Schemas ---

(def CreateAccountInput
  [:map
   [:client-id :string]]) ;; Snake_case pois vem do JSON da API assim

(def Account
  [:map
   [:id :string]
   [:client-id :string]
   [:balance :int]]) ;; Sempre use inteiros (centavos) para dinheiro!

;; --- Lógica ---

(defn create-account!
  [ds input]
  ;; 1. Validação
  (if-not (m/validate CreateAccountInput input)
    (throw (ex-info "Dados inválidos" {:errors (me/humanize (m/explain CreateAccountInput input))}))

    ;; 2. Preparação
    (let [new-account {:id        (str (UUID/randomUUID))
                       :client-id (:client-id input)
                       :balance   0}] ;; Começa zerada

      ;; 3. Persistência (Pode falhar se client-id não existir no banco)
      (try
        (db/insert-account! ds new-account)
        new-account
        (catch Exception e
          ;; Captura erro de chave estrangeira do Postgres
          (throw (ex-info "Erro ao criar conta. Verifique se o client-id existe."
                          {:cause (.getMessage e)})))))))

(defn find-by-id [ds id]
  (or (db/find-by-id ds id)
      (throw (ex-info "Conta não encontrada" {:id id :status 404}))))

;; Funções de apoio para Transação (serão usadas pelo componente Transaction)
(defn credit! [ds id amount]
  (db/update-balance! ds id amount))

(defn debit! [ds id amount]
  ;; Opcional: verificar saldo antes aqui, ou deixar o componente Transaction orquestrar
  (db/update-balance! ds id (* -1 amount))) ;; Soma negativo