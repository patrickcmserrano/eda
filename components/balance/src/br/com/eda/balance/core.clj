(ns br.com.eda.balance.core
  (:require [br.com.eda.balance.db :as db]))

(defn update-balance!
  [ds {:keys [account-id-from balance-from account-id-to balance-to]}]
  ;; Em uma transação real, poderíamos abrir uma Tx aqui.
  ;; Como estamos recebendo o estado FINAL do saldo, podemos fazer upserts independentes.
  ;; O evento BalanceUpdated traz os dois saldos.

  (when (and account-id-from balance-from)
    (db/upsert-balance! ds account-id-from balance-from))

  (when (and account-id-to balance-to)
    (db/upsert-balance! ds account-id-to balance-to)))

(defn get-balance
  [ds account-id]
  (db/find-by-account-id ds account-id))
