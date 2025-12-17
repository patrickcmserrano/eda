# Wallet Core (EDA Study)

Este projeto é uma implementação de um núcleo de transações financeiras (Wallet Core) baseada em Arquitetura Orientada a Eventos (EDA).

Foi desenvolvido como parte do desafio do curso Full Cycle, mas utilizando uma stack moderna e funcional com **Clojure** e **Polylith**, focando em imutabilidade, modularidade e separation of concerns.

## 🏗 Arquitetura & Stack

O projeto segue uma estrutura de Monorepo modular gerenciada pelo **Polylith**:

* **Linguagem:** Clojure (JDK 17+)
* **Gerenciamento de Workspace:** Polylith (`poly`)
* **Validação de Dados:** Malli
* **Banco de Dados:** PostgreSQL 15 (via `next.jdbc` e `HoneySQL`)
* **Mensageria:** Apache Kafka (via `Jackdaw`)
* **API:** Jetty + Reitit + Ring

### Estrutura do Workspace

```text
bases/
  └── wallet-api/       # API Gateway (REST -> Componentes)
components/
  ├── account/          # Domínio de Contas e Saldo
  ├── client/           # Domínio de Clientes
  ├── transaction/      # Core: Atomicidade e Orquestração
  ├── database/         # Infra: Connection Pool e Migrations
  └── kafka/            # Infra: Producers
projects/
  └── wallet/           # Artefato Deployável (Uberjar)

```

## 🚀 Como Rodar

### Pré-requisitos

* Docker & Docker Compose
* Clojure CLI
* Ferramenta `poly` (opcional, mas recomendada)

### 1. Subir Infraestrutura

Inicie o PostgreSQL, Zookeeper e Kafka:

```bash
docker compose up -d

```

### 2. Rodar a Aplicação (Modo Dev)

Você pode rodar diretamente via Clojure CLI a partir do projeto `wallet`:

```bash
cd projects/wallet
clojure -M -m br.com.eda.wallet-api.core

```

Ou, se preferir rodar tudo via Docker (Build Final):

```bash
docker compose up --build app

```

A API estará disponível em: `http://localhost:8080`

## 🧪 Testando a API

### Criar Cliente

```bash
curl -X POST http://localhost:8080/clients \
  -H "Content-Type: application/json" \
  -d '{"name": "Neo", "email": "neo@matrix.com"}'

```

### Criar Conta

Use o `id` retornado acima.

```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"client_id": "UUID_DO_CLIENTE"}'

```

### Realizar Transação

Isso debita da origem, credita no destino (Atomicamente) e publica no Kafka.

```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "account_id_from": "UUID_CONTA_ORIGEM",
    "account_id_to": "UUID_CONTA_DESTINO",
    "amount": 1000
  }'

```

## 🛠 Desenvolvimento

Para rodar os testes de todos os componentes:

```bash
clojure -M:poly test

```

Para verificar a integridade do workspace:

```bash
clojure -M:poly check

```

---

## ⚠️ Trade-offs e Melhorias Futuras

### Consistência de Dados (The Dual Write Problem)
A implementação atual do componente `Transaction` utiliza uma abordagem pragmática para o escopo deste exercício:
1. Commit da transação no PostgreSQL (Atomicidade garantida via `jdbc/with-transaction`).
2. Publicação do evento no Kafka (Fire and forget).

**Cenário de Risco:**
Existe uma janela de falha teórica (milissegundos) entre o commit do banco e a publicação no Kafka. Se o processo da aplicação for encerrado abruptamente (Crash/OOM/Falha de Rede) exatamente neste intervalo, o sistema entrará em estado inconsistente (Dinheiro debitado, mas evento não emitido).

**Solução para Produção:**
Para evoluir este projeto para um ambiente crítico, a solução recomendada seria implementar o **Transactional Outbox Pattern**:
1. Persistir o evento em uma tabela `outbox` dentro da mesma transação SQL da transferência.
2. Utilizar um processo assíncrono (Relay ou CDC com Debezium) para ler a tabela `outbox` e publicar no Kafka com garantia de entrega *At-Least-Once*.

### Outras Melhorias
* **Idempotência no Consumo:** Garantir que os consumidores Kafka lidem com mensagens duplicadas.
* **Schema Registry:** Adotar Avro ou JSON Schema para contrato estrito de mensagens.
* **Distributed Tracing:** Implementar OpenTelemetry para rastrear o fluxo entre API -> DB -> Kafka.