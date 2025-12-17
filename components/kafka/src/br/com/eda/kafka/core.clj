(ns br.com.eda.kafka.core
  (:require [jackdaw.client :as jc]
            [jsonista.core :as json]
            [jackdaw.data :as jd])
  (:import [org.apache.kafka.common.serialization StringSerializer StringDeserializer]))

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

(defn- consumer-config [bootstrap-servers group-id]
  {"bootstrap.servers"  bootstrap-servers
   "group.id"           group-id
   "auto.offset.reset"  "earliest" ;; Lê desde o início se não tiver offset salvo
   "enable.auto.commit" "true"
   "key.deserializer"   (.getName StringDeserializer)
   "value.deserializer" (.getName StringDeserializer)})

(defn new-consumer [config group-id]
  (let [servers (get config :bootstrap-servers "localhost:9092")]
    (jc/consumer (consumer-config servers group-id))))

(defn subscribe! [consumer topic-name]
  (jc/subscribe consumer [{:topic-name topic-name}]))

(defn poll!
  "Busca mensagens. Retorna um vetor de records."
  [consumer timeout-ms]
  (jc/poll consumer timeout-ms))