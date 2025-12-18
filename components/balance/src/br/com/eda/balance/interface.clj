(ns br.com.eda.balance.interface
  (:require [br.com.eda.balance.core :as core]
            [br.com.eda.balance.db :as db]))

(defn migrate! [ds]
  (db/migrate! ds))

(defn update-balance! [ds payload]
  (core/update-balance! ds payload))

(defn get-balance [ds account-id]
  (core/get-balance ds account-id))
