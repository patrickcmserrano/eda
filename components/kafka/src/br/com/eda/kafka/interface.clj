(ns br.com.eda.kafka.interface
  (:require [br.com.eda.kafka.core :as core]))

(defn new-producer
  "Cria um Kafka Producer conectado.
   Ex config: {:bootstrap-servers \"localhost:9092\"}"
  ([] (new-producer {}))
  ([config] (core/new-producer config)))

(defn produce!
  "Envia um mapa Clojure como JSON para o tópico especificado."
  [producer topic payload]
  (core/produce! producer topic payload))