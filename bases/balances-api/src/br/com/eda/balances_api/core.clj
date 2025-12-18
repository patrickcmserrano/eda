(ns br.com.eda.balances-api.core
  (:require [br.com.eda.balances-api.consumer :as consumer]
            [br.com.eda.balance.interface :as balance]
            [br.com.eda.database.interface :as db]
            [reitit.ring :as ring]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [muuntaja.core :as m]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

;; --- Handlers ---

(defn get-balance [ds]
  (fn [req]
    (let [id (get-in req [:path-params :id])]
      (if-let [bal (balance/get-balance ds id)]
        {:status 200
         :body   bal}
        {:status 404
         :body   {:error "Balance not found"}}))))

(defn app [ds]
  (ring/ring-handler
   (ring/router
    [""
     ["/balances/:id" {:get (get-balance ds)}]
     ["/health"       {:get (fn [_] {:status 200 :body {:status "ok"}})}]]
    {:data {:muuntaja   m/instance
            :middleware [muuntaja/format-middleware]}})))

;; --- Main ---

(defn -main [& args]
  (println ">>> [Balances-API] Starting...")

  (let [db-config    {:host     (System/getenv "DB_HOST")
                      :port     (some-> (System/getenv "DB_PORT") Integer/parseInt)
                      :user     (System/getenv "DB_USER")
                      :password (System/getenv "DB_PASS")
                      :dbname   (System/getenv "DB_NAME")}
        kafka-config {:bootstrap-servers (System/getenv "KAFKA_BOOTSTRAP_SERVERS")}
        port         (or (some-> (System/getenv "APP_PORT") Integer/parseInt) 3003)]

    ;; 1. Connect to DB
    (println ">>> [Balances-API] Connecting to DB...")
    (let [ds (db/get-datasource db-config)]

      ;; 2. Run Migrations
      (balance/migrate! ds)

      ;; 3. Start Consumer
      (consumer/start-consumer! kafka-config ds)

      ;; 4. Start HTTP Server
      (println ">>> [Balances-API] HTTP Server listening on port" port)
      (jetty/run-jetty (app ds) {:port port :join? true}))))
