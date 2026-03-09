# Cortex-M

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.X-4695EB?logo=quarkus)](https://quarkus.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-PG--Vector-336791?logo=postgresql)](https://www.postgresql.org/)
[![MCP](https://img.shields.io/badge/Protocol-MCP-blueviolet)](https://modelcontextprotocol.io/)
[![CloudEvents](https://img.shields.io/badge/CloudEvents-1.0-lightgrey?logo=cloudevents)](https://cloudevents.io/)
[![Build](https://img.shields.io/badge/build-Maven-C71A36?logo=apachemaven)](https://maven.apache.org/)

A cloud-native, modular, and token-efficient personal assistant framework built on open standards — independent of US
cloud providers.

***

## What is Cortex-M?

Cortex-M is a personal assistant microservice designed to be **affordable**, **stable**, **highly extensible**, and *
*independent of US cloud providers**.
Instead of relying on fragile instruction files and granting the agent broad system access, Cortex-M is built around
open standards — using the **Model Context Protocol (MCP)** for tools and a **vector database** for memory — keeping
token usage lean and behavior predictable. Native support for **MistralAI** ensures you can run cutting-edge models
without vendor lock-in.

The name reflects both its role as the central "brain" of your assistant infrastructure and its foundation on Quarkus —
the *Supersonic Subatomic Java* framework.

***

## ✨ Features

- **Provider-agnostic models** — Native support for **MistralAI** and other non-US providers. Deploy on your own
  infrastructure with zero vendor lock-in.
- **MCP-first tooling** — All agent capabilities are defined as structured MCP servers, registered in a database and
  dynamically loaded at startup or runtime.
- **Dynamic tool management** — Add or update MCP servers without redeploying the core service.
- **Connector architecture** — Interact with Cortex-M via any number of lightweight connector services (e.g., Matrix,
  Discord, Slack), written in any language.
- **Multi-connector support** — The agent handles multiple simultaneous connectors in parallel, making it ideal for
  cloud deployments.
- **Sandboxed execution** — The agent has no access to the local shell or file system; all capabilities are strictly
  scoped to registered MCP tools.
- **Vector memory** — A PG-Vector-backed memory store allows the agent to ingest and retrieve
  experiences as embeddings, replacing token-heavy history files with efficient semantic search.

***

## 📚 Documentation

See the [Wiki](https://github.com/workaround-org/cortex-m/wiki) for full documentation.

***

## 🚀 Getting Started

> Full setup instructions coming soon.

**Prerequisites:**

- Docker / Podman
- Java 25+

**Quick start:**

```bash
./mvnw quarkus:dev
```

**Quick start (Docker Compose):**

Coming soon!

```bash
docker compose up
```

***

## 🗺️ Roadmap

- [x] Dynamic MCP server registry (Postgres-backed)
- [x] Multi-connector support via WebSocket + CloudEvents JSON
- [x] Vector memory store (PG-Vector embeddings)
- [x] Direct runtime vector store access by the agent
- [x] Proactive task execution & scheduling (cron-based self-waking)
- [x] Soul / Personality initialization (interactive first-run setup & memory storage)
- [x] Matrix connector
- [x] Telegram connector
- [ ] Admin UI with Chat compontent

***

## 🧠 Design Principles

1. **Token efficiency** — Context is injected only when needed, not dumped wholesale.
2. **Open standards** — MCP, CloudEvents, WebSocket; no proprietary lock-in.
3. **Cloud-native** — Every component runs in a container; state lives in the database.
4. **Strict sandboxing** — The agent does only what its registered tools allow. Nothing more.
5. **Provider independence** — Support for non-US models and infrastructure; your data stays under your control.

***

## 📄 License

GPL3

***

*Cortex-M — The central intelligence for your microservice assistant.*
