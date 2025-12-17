(ns br.com.eda.account.interface
  (:require [br.com.eda.account.core :as core]
            [br.com.eda.account.db :as db]))

(defn create-account! [ds input]
  (core/create-account! ds input))

(defn find-by-id [ds id]
  (core/find-by-id ds id))

(defn credit! [ds id amount]
  (core/credit! ds id amount))

(defn debit! [ds id amount]
  (core/debit! ds id amount))

(defn list-all [ds]
  (db/list-all ds))
