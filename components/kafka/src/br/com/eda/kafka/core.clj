(ns br.com.eda.kafka.core
  (:require [jackdaw.client :as jc]
            [jsonista.core :as json])
  (:import [org.apache.kafka.common.serialization StringSerializer]))

;; --- Configuração do Producer ---

(defn- producer-config [bootstrap-servers]
  {"bootstrap.servers" bootstrap-servers
   "acks"              "all"
   "retries"           (int 0)
   "key.serializer"    (.getName StringSerializer)
   "value.serializer"  (.getName StringSerializer)})

(defn new-producer [config]
  (let [servers (get config :bootstrap-servers "localhost:9092")]
    (jc/producer (producer-config servers))))

;; --- Envio de Mensagens ---

(defn produce!
  "Converte o payload para JSON String e envia via Jackdaw."
  [producer topic-name payload]
  (let [json-payload (json/write-value-as-string payload)
        ;; A CORREÇÃO: Jackdaw exige que o tópico seja um mapa com :topic-name
        topic-config {:topic-name topic-name}]

    ;; jc/produce! retorna um Future. Usamos @ para bloquear e garantir o envio.
    @(jc/produce! producer topic-config json-payload)

    (println ">>> Kafka Event sent to topic:" topic-name)))