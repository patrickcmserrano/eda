(ns user
  (:require [integrant.repl :as ig-repl]
            [integrant.core :as ig]
            [br.com.eda.wallet-api.core :as main] ; Seu entrypoint
            [br.com.eda.database.interface :as db]
            [br.com.eda.kafka.interface :as kafka]))

;; Configuração do Sistema para Desenvolvimento
(def config
  {::ds {:host "localhost" :port 5432 :user "root" :password "root" :dbname "wallet"}
   ::kp {:bootstrap-servers "localhost:9092"}
   ::server {:port 8080 :ds (ig/ref ::ds) :kp (ig/ref ::kp)}})

;; Como iniciar cada componente
(defmethod ig/init-key ::ds [_ config]
  (println "🔵 Iniciando Banco de Dados...")
  (db/get-datasource config))

(defmethod ig/init-key ::kp [_ config]
  (println "Modo dev, simulando conexao kafka...")
  (kafka/new-producer config))

(defmethod ig/init-key ::server [_ {:keys [port ds kp]}]
  (println "🟢 Iniciando Servidor Jetty na porta" port "...")
  ;; Importante: :join? false para não travar o REPL
  (br.com.eda.wallet-api.core/app ds kp))
;; Nota: Aqui precisaríamos ajustar seu core.clj para retornar o handler ou o server object
;; Para facilitar hoje, vamos focar apenas em recarregar o código.

;; Comandos para você usar no REPL
(defn go []
  (ig-repl/set-prep! (constantly config))
  (ig-repl/go))

(defn halt []
  (ig-repl/halt))

(defn reset []
  (ig-repl/reset))

(comment
  (go)    ;; Inicia tudo
  (halt)  ;; Para tudo
  (reset) ;; RECARREGA TODO O CÓDIGO MODIFICADO e reinicia o sistema
  )