(ns br.com.eda.transaction.core
  (:require [br.com.eda.transaction.db :as db]
            [br.com.eda.account.interface :as account]
            [br.com.eda.kafka.interface :as kafka]
            [next.jdbc :as jdbc]
            [malli.core :as m]
            [malli.error :as me])
  (:import [java.util UUID]
           [java.sql Timestamp]))

;; --- Schemas ---

(def CreateTransactionInput
  [:map
   [:account-id-from :string]
   [:account-id-to :string]
   [:amount [:int {:min 1}]]])

;; --- Lógica ---

(defn create-transaction!
  [{:keys [datasource kafka-producer]} input]

  (if-not (m/validate CreateTransactionInput input)
    (throw (ex-info "Dados inválidos" {:errors (me/humanize (m/explain CreateTransactionInput input))}))

    (let [tx-id   (str (UUID/randomUUID))
          ;; --- A CORREÇÃO AQUI ---
          ;; Convertemos Instant -> Timestamp (JDBC entende isso)
          now     (Timestamp/from (java.time.Instant/now))

          tx-data (assoc input :id tx-id :created-at now)]

      (jdbc/with-transaction [tx datasource]
        (let [acc-from-id (:account-id-from input)
              acc-to-id   (:account-id-to input)
              amount      (:amount input)]

          (let [account-from (account/find-by-id tx acc-from-id)]
            (if (< (:balance account-from) amount)
              (throw (ex-info "Saldo insuficiente"
                              {:status   422
                               :current  (:balance account-from)
                               :required amount}))
              (do
                (account/debit! tx acc-from-id amount)
                (account/credit! tx acc-to-id amount)
                (db/insert-transaction! tx tx-data))))))

      (let [kafka-event {:event-name "TransactionCreated"
                         :payload    tx-data}]
        (kafka/produce! kafka-producer "transactions" kafka-event))

      tx-data)))