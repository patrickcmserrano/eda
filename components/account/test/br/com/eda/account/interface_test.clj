(ns br.com.eda.account.interface-test
  (:require [clojure.test :refer [deftest is testing]]
            [br.com.eda.account.interface :as account]
            [br.com.eda.client.interface :as client]
            [br.com.eda.database.interface :as db]))

;; Helper para garantir uma conexão fresca
(defn test-ds []
  (db/get-datasource))

(deftest account-flow-test
  (let [ds (test-ds)]

    (testing "Deve criar conta com saldo zero para um cliente existente"
      ;; 1. Setup: Precisamos de um cliente válido (FK)
      (let [client    (client/create-client! ds {:name  "Tester"
                                                 :email "test@account.com"})
            client-id (:id client)]

        ;; 2. Ação: Criar Conta
        (let [acc (account/create-account! ds {:client-id client-id})]

          ;; 3. Asserções (Verificações)
          (is (some? (:id acc)) "A conta deve ter um ID gerado")
          (is (= client-id (:client-id acc)) "A conta deve pertencer ao cliente correto")
          (is (= 0 (:balance acc)) "O saldo inicial deve ser zero")

          ;; Teste aninhado: Movimentação
          (testing "Deve creditar e debitar corretamente"
            (account/credit! ds (:id acc) 500)
            (let [updated (account/find-by-id ds (:id acc))]
              (is (= 500 (:balance updated)) "Saldo deve ser 500 após crédito"))

            (account/debit! ds (:id acc) 200)
            (let [updated (account/find-by-id ds (:id acc))]
              (is (= 300 (:balance updated)) "Saldo deve ser 300 após débito"))))))))

(deftest validation-test
  (let [ds (test-ds)]
    (testing "Não deve criar conta sem client-id"
      (is (thrown? clojure.lang.ExceptionInfo
                   (account/create-account! ds {}))))))