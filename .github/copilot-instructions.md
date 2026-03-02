# Cortex-M — Copilot Instructions

## Git Commits

Always use [GitMoji](https://gitmoji.dev/) at the start of every commit message.

```
✨ feat: add recurring task delete endpoint
🐛 fix: handle null soul on first startup
♻️ refactor: extract job builder to helper method
✅ test: add TaskBean integration tests
📝 docs: update copilot instructions
```

## Build & Test Commands

```bash
# Dev mode (starts Quarkus with live reload + DevServices Postgres)
./mvnw quarkus:dev

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=TaskBeanTest

# Run a single test method
./mvnw test -Dtest=TaskBeanTest#addTrigger_persistsOneShotTask

# Package (used by CI)
./mvnw clean package
```

Java 25 is required. CI builds and publishes to GHCR via `docker-build-push.yml`.

## Architecture Overview

Cortex-M is a Quarkus-based AI assistant microservice. The core flow is:

1. **Connectors** (external services: Matrix, Telegram, etc.) connect via WebSocket (`ConnectorWS`) using the CloudEvents 1.0 envelope format.
2. **`CortexMService`** routes each message to **`CortexMBot`**, a LangChain4j `@RegisterAiService` interface with three methods: `onboard`, `chat`, and `executeTask`.
3. **`CortexMBot`** uses **`CortexMToolProvider`** (a custom `ToolProvider`) instead of the default LangChain4j tool wiring. Tools are gathered from two sources:
   - **Built-in tools**: all CDI beans implementing `CortexMTool` (marker interface), auto-collected via `@All List<CortexMTool>`
   - **MCP tools**: `McpHttpConfig` records loaded from Postgres at startup, each spawning a `DefaultMcpClient`
4. **`SoulTool`** is injected separately and only added to the provider if no soul record exists yet (first-run onboarding guard).
5. **Scheduled tasks** (`TaskBean` + Quartz) execute autonomously, calling `CortexMBot.executeTask` and broadcasting results via `ConnectorWS.broadCast`.

### Key Packages

| Package | Role |
|---|---|
| `bot/` | AI service interface, model supplier, chat routing service |
| `connector/` | CloudEvent DTOs, session management |
| `database/` | JPA entities + Panache repositories |
| `memory/` | `PostgresChatMemoryStore` — chat history via Panache/PG |
| `scheduler/` | Quartz integration (`TaskBean`, `MyJob`, `RecurringSchedule`) |
| `tools/` | `CortexMToolProvider`, `CortexMToolProviderSupplier` |
| `tools/buildin/` | Built-in `@Tool` classes: `TaskTool`, `SoulTool`, `MemoryTool`, `McpConnectionTool` |

## Key Conventions

### Adding a New Built-in Tool
1. Create a class in `tools/buildin/` annotated `@ApplicationScoped`.
2. Implement the `CortexMTool` marker interface.
3. Annotate public methods with LangChain4j `@Tool("…")`. Write tool descriptions as LLM-facing instructions — be explicit about parameter formats (e.g., ISO-8601 timestamps, 6-field Quartz cron).
4. Inject `TaskBean` or other services; **do not** inject `CortexMBot` directly (circular dependency risk).
5. No registration needed — `CortexMToolProvider` picks it up via `@All List<CortexMTool>`.

### Scheduled Tasks
- **`TaskBean`** owns both the Quartz scheduler and `ScheduledTaskRepository`. Always go through `TaskBean`; never schedule Quartz jobs directly.
- Two task types: `TYPE_ONE_SHOT` ("ONE_SHOT") and `TYPE_CRON` ("CRON") — these string constants live in `TaskBean` and are mirrored as literals in `ScheduledTask.toString()`.
- `RecurringSchedule` is a record that validates the Quartz cron expression (6 fields: `seconds minutes hours dom month dow`) in its compact constructor.
- One-shot tasks past their execution window are silently pruned on startup (`restorePersistedTasks`).
- `MyJob` gets a new instance per execution (Quartz default); it reads the prompt from `JobDetail.description` and task metadata from `JobDataMap`.

### LLM Prompts (`CortexMBot`)
- `onboard`: first-message persona setup.
- `chat`: injects `{soul}` (personality text from DB) and `{date}` (current UTC instant) into the system prompt.
- `executeTask`: called by the scheduler; no user interaction — act and report, no clarifying questions.
- The model is configured via `de.u_project.cortex-m.ollama.model-name` (env: `OLLAMA_MODEL_NAME`). Use this custom property for runtime config; the built-in `quarkus.langchain4j.ollama.chat-model.model-name` is baked into native images.

### Session & Memory
- Sessions are UUID strings persisted in the `Session` table. `ConnectorWS` validates session on open.
- Chat memory is keyed by the WebSocket `session` path param and stored as JSON in `ChatMemory`.

### Database
- Panache repositories extend `PanacheRepository<Entity, Id>`.
- `import.sql` seeds a dev session, a soul record, and an example MCP config. It runs automatically in dev mode.
- Schema is managed by Hibernate (`quarkus.hibernate-orm.database.generation` defaults to `update`).

## Testing Conventions

- **`@QuarkusTest`** for integration tests — Quarkus DevServices spins up a real Postgres container automatically; no manual DB setup needed.
- **Only mock `CortexMBot` and `ConnectorWS`** — everything else (Panache, Quartz, repositories) runs against the real stack.
- Use `io.quarkus.test.InjectMock` (not `io.quarkus.test.junit.mockito.InjectMock` — that class does not exist in this project's version).
- `CortexMBot` and `ConnectorWS` are `@Singleton` (pseudo-scope); add `@MockitoConfig(convertScopes = true)` on each `@InjectMock` field to allow Quarkus to proxy them.
- Quartz does not participate in JTA transactions — avoid `@TestTransaction`. Use `@BeforeEach` + `repository.deleteAll()` inside a `@Transactional` method instead.
- Plain unit tests (no Quarkus context) are fine for pure-logic classes like `RecurringSchedule` and `ScheduledTask`.
