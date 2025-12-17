(ns br.com.eda.transaction.interface
  (:require [br.com.eda.transaction.core :as core]))

(defn create-transaction!
  "Cria transação. Input deve usar kebab-case:
   {:account-id-from \"...\" :account-id-to \"...\" :amount 100}"
  [deps input]
  (core/create-transaction! deps input))