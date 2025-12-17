(ns br.com.eda.event-processor.core
  (:require [br.com.eda.kafka.interface :as kafka]
            [jsonista.core :as json])
  (:gen-class))

(defn process-transaction [json-value]
  (try
    ;; O Kafka entrega uma String, precisamos converter para JSON/Map
    (let [event (json/read-value json-value json/keyword-keys-object-mapper)
          payload (:payload event)]

      (println "---------------------------------------------------")
      (println "📥 [Worker] Evento Recebido:" (:event-name event))
      (println "💰 Valor:" (:amount payload))
      (println "origin:" (:account-id-from payload))
      (println "destino:" (:account-id-to payload))
      (println "---------------------------------------------------"))
    (catch Exception e
      (println "❌ Erro ao processar mensagem:" (.getMessage e)))))

(defn start-processor! [config]
  (println ">>> Iniciando Worker (Event Processor)...")
  (let [consumer (kafka/new-consumer config "wallet-worker-group")]

    (kafka/subscribe! consumer "transactions")

    (println ">>> Worker escutando tópico 'transactions'...")

    ;; Loop Infinito de Processamento
    (while true
      (let [records (kafka/poll! consumer 5000)] ;; Espera até 5s por mensagens
        (doseq [record records]
          (let [value (:value record)]
            (process-transaction value)))))))

(defn -main [& args]
  (let [kafka-servers (or (System/getenv "KAFKA_BOOTSTRAP_SERVERS") "localhost:9092")]
    (start-processor! {:bootstrap-servers kafka-servers})))