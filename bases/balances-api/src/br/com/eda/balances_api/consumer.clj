(ns br.com.eda.balances-api.consumer
  (:require [br.com.eda.kafka.interface :as kafka]
            [br.com.eda.balance.interface :as balance]
            [clojure.tools.logging :as log]
            [jsonista.core :as json]))

(def mapper (json/object-mapper {:decode-key-fn true}))

(defn start-consumer! [kafka-config db-ds]
  (let [consumer (kafka/new-consumer kafka-config "balances-service")]
    (log/info ">>> [Consumer] Subscribing to topic 'balances'...")
    (kafka/subscribe! consumer "balances")

    (future
      (try
        (loop []
          (let [records (kafka/poll! consumer 100)]
            (doseq [record records]
              (let [value-str (get record :value)]
                (log/info ">>> [Consumer] Received update:" value-str)
                (try
                  (let [value-map (json/read-value value-str mapper)
                        payload   (:payload value-map)]

                    (if payload
                      (balance/update-balance! db-ds payload)
                      (log/warn "!!! [Consumer] Received message without payload:" value-map)))

                  (catch Exception e
                    (log/error e "!!! [Consumer] Error updating balance"))))))
          (recur))
        (catch Exception e
          (log/error e "!!! [Consumer] Fatal error in consumer loop"))
        (finally
          (.close consumer))))))
