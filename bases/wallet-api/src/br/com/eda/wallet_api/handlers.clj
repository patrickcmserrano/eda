(ns br.com.eda.wallet-api.handlers
  (:require [br.com.eda.client.interface :as client]
            [br.com.eda.account.interface :as account]
            [br.com.eda.transaction.interface :as transaction]))

(defn health [_]
  {:status 200 :body {:status "ok"}})

;; --- CLIENT ---
(defn create-client [ds]
  (fn [request]
    (let [input (:body-params request)
          result (client/create-client! ds input)]
      {:status 201
       :body   {:id         (:id result)
                :name       (:name result)
                :email      (:email result)
                :created_at (:created-at result)}}))) ;; Kebab -> Snake manual

;; --- ACCOUNT ---
(defn create-account [ds]
  (fn [request]
    (let [body (:body-params request)
          ;; Entrada: Snake (JSON) -> Kebab (Componente)
          input {:client-id (:client_id body)}

          result (account/create-account! ds input)]
      {:status 201
       ;; Saída: Kebab (Componente) -> Snake (JSON)
       :body {:id (:id result)
              :client_id (:client-id result)
              :balance (:balance result)}})))

(defn get-balance [ds]
  (fn [request]
    (let [id (get-in request [:path-params :id])
          account (account/find-by-id ds id)]
      {:status 200
       :body {:id (:id account)
              :balance (:balance account)}})))

;; --- TRANSACTION ---
(defn create-transaction [deps]
  (fn [request]
    (let [body (:body-params request)
          ;; Entrada: Snake (JSON) -> Kebab (Componente)
          input {:account-id-from (:account_id_from body)
                 :account-id-to   (:account_id_to body)
                 :amount          (:amount body)}

          result (transaction/create-transaction! deps input)]
      {:status 201
       ;; Saída: Kebab (Componente) -> Snake (JSON)
       :body {:id (:id result)
              :account_id_from (:account-id-from result)
              :account_id_to   (:account-id-to result)
              :amount          (:amount result)
              :created_at      (:created-at result)}})))


(defn list-clients [ds]
  (fn [_]
    (let [clients (client/list-all ds)
          ;; Formata a lista para Snake Case
          response (map (fn [c] 
                          {:id (:clients/id c)
                           :name (:clients/name c)
                           :email (:clients/email c)
                           :created_at (:clients/created_at c)})
                        clients)]
      {:status 200 :body response})))

(defn list-accounts [ds]
  (fn [_]
    (let [accounts (account/list-all ds)
          response (map (fn [a]
                          {:id (:accounts/id a)
                           :client_id (:accounts/client_id a)
                           :balance (:accounts/balance a)})
                        accounts)]
      {:status 200 :body response})))

(defn get-statement [ds]
  (fn [request]
    (let [id (get-in request [:path-params :id])
          txs (transaction/get-statement ds id)
          response (map (fn [t]
                          {:id (:transactions/id t)
                           :account_id_from (:transactions/account_id_from t)
                           :account_id_to   (:transactions/account_id_to t)
                           :amount          (:transactions/amount t)
                           :created_at      (:transactions/created_at t)})
                        txs)]
      {:status 200 :body response})))