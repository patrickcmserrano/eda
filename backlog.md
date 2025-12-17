###🏁 Fase 1: Setup & Infraestrutura Base*O objetivo aqui é ter o ambiente rodando e os componentes utilitários prontos.*

* [ ] **1.1. Inicializar Workspace Polylith**
* Criar diretório `eda-study`.
* Configurar `deps.edn` raiz e `workspace.edn`.
* Gerar estrutura inicial com `poly` (pastas bases, components, projects).


* [ ] **1.2. Levantar Infraestrutura (Docker)**
* Criar `docker-compose.yaml` na raiz.
* Adicionar serviço **PostgreSQL** (porta 5432).
* Adicionar serviços **Kafka + Zookeeper** (porta 9092).
* Adicionar **Control Center** (opcional, para visualizar tópicos).


t

* [ ] **1.4. Componente `kafka` (Infra)**
* Criar componente: `poly create component name:kafka`.
* Adicionar deps: `jackdaw`, `jsonista`.
* Implementar `interface.clj`: Função `produce!` que recebe um tópico e um payload (mapa) e envia como JSON.



---

###🧱 Fase 2: Domínios Simples (CRUD)*Implementar a lógica de negócio isolada da API HTTP.*

* [ ] **2.1. Componente `client**`
* Criar componente: `poly create component name:client`.
* **Schema:** Definir `ClientSchema` com Malli (ID, Name, Email, CreatedAt).
* **DB:** Implementar função `insert-client!` usando HoneySQL.
* **Interface:** Expor função `create-client` que valida o input, gera ID (UUID) e salva no banco.


* [ ] **2.2. Componente `account**`
* Criar componente: `poly create component name:account`.
* **Schema:** Definir `AccountSchema` (ID, ClientID, Balance, CreatedAt).
* **DB:** Implementar `insert-account!` e `find-by-id`.
* **Interface:** Expor `create-account` (valida se client existe - *mock por enquanto ou query direta* - e salva).
* **Interface:** Expor `get-balance` (busca saldo pelo ID da conta).



---

###⚙️ Fase 3: O Coração (Transaction & Atomicidade)*A parte mais crítica para a aprovação.*

* [ ] **3.1. Componente `transaction**`
* Criar componente: `poly create component name:transaction`.
* **Schema:** Definir `TransactionSchema` (ID, AccountFrom, AccountTo, Amount).
* **Integração:** Adicionar dependências dos componentes `account` e `kafka` no `deps.edn` de `transaction`.


* [ ] **3.2. Lógica de Negócio (Atomicidade)**
* Implementar função `create-transaction!` que recebe `datasource` e `kafka-producer`.
* **Passo 1:** Abrir transação JDBC (`with-transaction`).
* **Passo 2:** Buscar contas (origem e destino) e validar saldo.
* **Passo 3:** Executar `account/update-balance!` (crédito e débito) dentro da tx.
* **Passo 4:** Inserir registro na tabela `transactions`.


* [ ] **3.3. Disparo de Evento**
* Após o commit do banco (ou dentro, dependendo da estratégia simples), chamar `kafka/produce!`.
* **Formato:** Garantir que o payload JSON seja `{ "Name": "TransactionCreated", "Payload": { ... } }`.



---

###🌐 Fase 4: API HTTP (A Camada de Entrada)*Expor tudo para o mundo via REST.*

* [ ] **4.1. Base `wallet-api**`
* Configurar deps: `ring`, `reitit`, `muuntaja` (para JSON).
* Configurar middleware para converter JSON de request (kebab-case) e response (snake_case - **Importante para compatibilidade com Go**).


* [ ] **4.2. Handlers HTTP**
* Implementar `POST /clients` -> Chama `client/create-client`.
* Implementar `POST /accounts` -> Chama `account/create-account`.
* Implementar `GET /accounts/{id}/balance` -> Chama `account/get-balance`.
* Implementar `POST /transactions` -> Chama `transaction/create-transaction`.


* [ ] **4.3. Servidor Web**
* Configurar Jetty/Ring para iniciar na porta **8080**.
* Injetar dependências (DataSource e Kafka Producer) no sistema (usando `integrant` ou passando manualmente no `main`).



---

###🚀 Fase 5: Integração e Entrega*Juntar tudo e garantir que funciona.*

* [ ] **5.1. Projeto `wallet` (Artefato)**
* Configurar `projects/wallet/deps.edn`.
* Incluir base `wallet-api` e todos os componentes.
* Criar namespace `br.com.eda.wallet.main` (`-main`).


* [ ] **5.2. Arquivo `client.http**`
* Criar um arquivo `.http` (igual ao do VS Code do professor) para testar as chamadas manualmente.


* [ ] **5.3. Testes Manuais Finais**
* Subir banco e kafka (`docker compose up`).
* Rodar o projeto: `clj -M:wallet`.
* Criar Cliente -> Criar 2 Contas -> Fazer Transferência.
* Verificar se o saldo atualizou no banco.
* Verificar se a mensagem chegou no Kafka (usando `kcat` ou Control Center).



---

