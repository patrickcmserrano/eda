(ns br.com.eda.wallet-api.core
  (:require [br.com.eda.wallet-api.handlers :as h]
            [br.com.eda.database.interface :as db]
            [br.com.eda.kafka.interface :as kafka]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [ring.adapter.jetty :as jetty]
            [nrepl.server :refer [start-server stop-server]])
  (:gen-class)) ;; Necessário para gerar o .jar executável

;; Configuração do JSON (Snake Case <-> Keywords)
(def muuntaja-instance
  (m/create m/default-options))

(defonce server-ref (atom nil))

(defn app [ds kp]
  (ring/ring-handler
   (ring/router
    [""
     ["/health" {:get h/health}]

     ;; Clients
     ["/clients" {:post (h/create-client ds)
                  :get  (h/list-clients ds)}] ;; <--- NOVO

     ;; Accounts
     ["/accounts" {:post (h/create-account ds)
                   :get  (h/list-accounts ds)}] ;; <--- NOVO

     ["/accounts/:id/balance" {:get (h/get-balance ds)}]
     ["/accounts/:id/credit"  {:post (h/credit-account ds)}]

     ;; Extrato (Transactions History)
     ["/accounts/:id/transactions" {:get (h/get-statement ds)}] ;; <--- NOVO

     ;; Transactions
     ["/transactions" {:post (h/create-transaction {:datasource     ds
                                                    :kafka-producer kp})}]]

    {:data {:muuntaja   muuntaja-instance
            :middleware [muuntaja/format-middleware]}})))



;; --- Funções de Ciclo de Vida (Start/Stop) ---

(defn start-server!
  "Inicia o servidor e salva a instância no atom.
   Se join? for false, não bloqueia o REPL."
  [& {:keys [port join? db-config kafka-config]
      :or {port 8080 join? false db-config {} kafka-config {}}}]

  (if @server-ref
    (println "⚠️ Servidor já está rodando!")
    (let [ds (db/get-datasource db-config)
          kp (kafka/new-producer kafka-config)]

      (println ">>> Servidor iniciado na porta" port "🚀")

      (let [instance (jetty/run-jetty (app ds kp) {:port port :join? join?})]
        (reset! server-ref instance)))))

;; --- nREPL ---

(defonce nrepl-ref (atom nil))

(defn start-nrepl!
  "Inicia o servidor nREPL na porta especificada."
  [& {:keys [port] :or {port 7000}}]
  (if @nrepl-ref
    (println "⚠️ nREPL já está rodando!")
    (let [server (start-server :port port)]
      (println ">>> nREPL iniciado na porta" port "⚡")
      (reset! nrepl-ref server))))

(defn stop-nrepl! []
  (if-let [server @nrepl-ref]
    (do
      (stop-server server)
      (reset! nrepl-ref nil)
      (println ">>> nREPL parado. 🛑"))
    (println "⚠️ Nenhum nREPL rodando para parar.")))

(defn stop-server! []
  (if-let [server @server-ref]
    (do
      (.stop server)
      (reset! server-ref nil)
      (println ">>> Servidor parado. 🛑"))
    (println "⚠️ Nenhum servidor rodando para parar.")))

(defn restart-server! []
  (stop-server!)
  (start-server!))

;; --- Entrypoint (Docker/Produção) ---

(defn -main [& _args]
  (let [db-config {:host     (System/getenv "DB_HOST")
                   :port     (some-> (System/getenv "DB_PORT") (Integer/parseInt))
                   :user     (System/getenv "DB_USER")
                   :password (System/getenv "DB_PASS")
                   :dbname   (System/getenv "DB_NAME")}
        kafka-config {:bootstrap-servers (System/getenv "KAFKA_BOOTSTRAP_SERVERS")}
        port (some-> (System/getenv "APP_PORT") (Integer/parseInt))]

    (start-nrepl!)
    ;; Em produção, queremos join? true para manter o container vivo
    (start-server! :join? true
                   :port (or port 8080)
                   :db-config db-config
                   :kafka-config kafka-config)))