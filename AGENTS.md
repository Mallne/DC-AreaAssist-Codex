# DiCentra AreaAssist Codex

## Project Overview

**AreaAssist Codex** is the backend server for the AreaAssist ecosystem — a Ktor (Netty) application built on the
**Synapse** framework. It provides service discovery, server-side action storage, and differential sync for
AreaAssist clients.

### Repository Structure

Codex lives in `areaassist/server/` within the DiCentra Application Framework monorepo (private). The monorepo is assembled via **git submodules** — each top-level module is its own repository.

Codex has its own **open-source** repository:
- **GitHub:** [Mallne/DC-AreaAssist-Codex](https://github.com/Mallne/DC-AreaAssist-Codex)
- **Clone:** `git clone git@github.com:Mallne/DC-AreaAssist-Codex.git`

> The standalone repo cannot build in isolation — it depends on sibling modules (`aviator`, `synapse`, `areaassist/shared`). Those artifacts must be published to a Maven repository or linked via Gradle composite builds.

### Relationship to Synapse

Codex is **not** part of the Synapse framework. It is an AreaAssist component that **depends on** Synapse as a library
(`dc.synapse.core`) to reuse base infrastructure:

- Authentication (OIDC / optional)
- Database connectivity & migrations (Exposed + PostgreSQL)
- Routing primitives (OpenAPI discovery, DiscoveryGenerator)
- Catalyst MCP gateway

### Main Technologies

- **Language:** Kotlin (JVM)
- **Framework:** Ktor (Netty engine)
- **Dependency Injection:** Koin (with KSP-generated modules + manual `module {}` blocks)
- **Persistence:** PostgreSQL (via Exposed ORM, Flyway-style migrations)
- **Serialization:** Kotlin Serialization (JSON)
- **Security:** OIDC-based authentication (optional, delegated to Synapse)
- **Core Libraries:** `dc.synapse.core`, `dc.areaassist.shared`

### Architecture

- **Entry Point:** `Application.codexModule()` — installs Koin, configures DB/security/HTTP, registers routes
- **Koin Modules:** `DI` (Synapse base), `CodexDI` (SyncRepository, SyncService), `DCAAAppModule` (component scan)
- **Routing:** Organized into 4 route files:
  - `Builtin.kt` — `/services/builtin`, `/services/builtin/ingest`
  - `Health.kt` — `/health`
  - `StoredSearch.kt` — `/bundle/*` (server-side actions CRUD)
  - `SyncRoutes.kt` — `/sync`, `/sync/attestations`
  - Plus all Synapse base routes (`/services`, `/catalyst`, `/scope`, `/discovery`)
- **Database Tables (4):** `apiservicedata`, `scopes`, `actions`, `sync_entries`
- **Services:** `ActionsService` (actions CRUD), `SyncService` (sync facade), plus Synapse's `APIDBService` and `ScopeService`

---

## Building and Running

### Prerequisites

- JDK 17+
- Docker (for PostgreSQL)

### Key Commands

```bash
# Build the project
./gradlew build

# Run the server (development)
./gradlew run

# Create a fat JAR (Shadow JAR)
./gradlew shadowJar

# Run Tests
./gradlew test

# Start Dependencies (Postgres)
docker-compose up -d
```

---

## Development Conventions

### Coding Style

- Follows standard Kotlin idiomatic patterns.
- New services should be added to the Koin module in `DCAAAppModule.kt` (either via `@Single` annotation scan or
  manual `CodexDI` module).
- New routes should be registered in `Application.codexModule()` as top-level `fun Application.foo()` calls.

### Security & Authentication

- Authentication is handled via Ktor's `authenticate` blocks (delegated to Synapse's `configureSecurity()`).
- Authorization levels: `user`, `admin`, `superadmin`.
- Scope-based filtering enforced for service and action access.
- The `/services/builtin/ingest` endpoint requires SuperAdmin.

### Database Migrations

- Migrations are located in `src/main/resources/db/migration/`.
- Follow the `V<Version>__<Description>.sql` naming convention.
- Migrations are automatically applied on startup via Synapse's `configureDatabase` hook.
- The initial migration (`V0__create.generated.sql`) is auto-generated — use `DATA_AUTOCREATEDELTA` for delta migrations.

### API Response Structure

- Standardized responses use `@ResponseObject` annotation and `DiscoveryResponse` wrapper for Synapse-base routes.
- Codex-specific routes use plain types: `ServersideActionHolder`, `ActionDTO`, `SyncAggregateResponse`, etc.
- All Codex-specific routes carry `x-dicentra-aviator-serviceDelegateCall` OpenAPI extensions.

---

## Configuration

The server is configured via environment variables, which override the defaults in
`src/main/resources/application.yaml`.

### Database Configuration (`data:`)

| Variable               | Description                                                  | Default                |
|------------------------|--------------------------------------------------------------|------------------------|
| `DATA_URL`             | JDBC Connection URL (e.g., `jdbc:postgresql://host:port/db`) | **Required**           |
| `DATA_USER`            | Database username                                            | **Required**           |
| `DATA_PASSWORD`        | Database password                                            | **Required**           |
| `DATA_SCHEMA`          | Database schema to use                                       | `codex`                |
| `DATA_AUTOCREATEDELTA` | Automatically create delta migrations                        | `false`                |
| `DATA_MIGRATIONNAME`   | Name of the initial migration file                           | `V0__create.generated` |

### Server Configuration (`server:`)

| Variable             | Description                           | Default                       |
|----------------------|---------------------------------------|-------------------------------|
| `AUTORELEASEVERSION` | Enable auto-release versioning        | `true`                        |
| `TLSENABLED`         | Enable TLS/HTTPS                      | `true`                        |
| `HOSTNAME`           | Server hostname                       | (empty)                       |
| `INFO`               | Server information string             | `DiCentra AreaAssist Codex`   |
| `DESCRIPTION`        | Server description                    | `A discovery for AreaAssist.` |
| `BASE_LOCATOR`       | Aviator base service locator prefix   | `DCAACodex`                   |
| `CORS_ALL`           | Allow all CORS origins                | `false`                       |
| `CORS_HOSTS`         | Specific CORS hosts (comma-separated) | (empty)                       |

### Catalyst (MCP/LLM) Configuration (`catalyst:`)

| Variable             | Description                       | Default            |
|----------------------|-----------------------------------|--------------------|
| `CATALYST_ENABLED`   | Enable Catalyst integration       | `true`             |
| `CATALYST_ANONYMOUS` | Allow anonymous Catalyst requests | `true`             |
| `CATALYST_TITLE`     | Catalyst service title            | `Synapse Catalyst` |

### Security Configuration (`security:`)

| Variable                          | Description                   | Default |
|-----------------------------------|-------------------------------|---------|
| `SECURITY_ENABLED`                | Enable OIDC Authentication    | `false` |
| `SECURITY_ISSUER`                 | OIDC Provider Issuer URL      | (empty) |
| `SECURITY_CLIENTID`               | OIDC Client ID                | (empty) |
| `SECURITY_CLIENTSECRET`           | OIDC Client Secret            | (empty) |
| `SECURITY_GROUPS_USER`            | OIDC group for standard users | (empty) |
| `SECURITY_GROUPS_SUPERADMIN`      | OIDC group for superadmins    | (empty) |
| `SECURITY_AREAASSIST_CLIENTID`    | AreaAssist OIDC client ID     | (empty) |
| `SECURITY_AREAASSIST_CLIENTNAME`  | AreaAssist OIDC client name   | `Authentication` |

### AreaAssist-specific

| Variable                              | Default  | Description |
|---------------------------------------|----------|-------------|
| `SECURITY_AREAASSIST_ACCOUNTCONSOLE`  | `""`     | Relative path to OIDC account console |
| `PREFERRED_TRANSFORM`                 | `Native` | Default transform for service calls |

---

## Key Files & Directories

- `src/main/kotlin/.../codex/Application.kt` — Main entry point and `codexModule()` definition
- `src/main/kotlin/.../codex/di/DCAAAppModule.kt` — Koin DI: `DCAAAppModule`, `CodexDI`
- `src/main/kotlin/.../codex/model/Config.kt` — AreaAssist-specific config + OIDC OpenAPI generation
- `src/main/kotlin/.../codex/model/ActionDTO.kt` — Server-side action data model
- `src/main/kotlin/.../codex/routes/Builtin.kt` — `/services/builtin` endpoints
- `src/main/kotlin/.../codex/routes/Health.kt` — `/health` endpoint
- `src/main/kotlin/.../codex/routes/StoredSearch.kt` — `/bundle/*` CRUD
- `src/main/kotlin/.../codex/routes/SyncRoutes.kt` — `/sync`, `/sync/attestations`
- `src/main/kotlin/.../codex/service/ActionsService.kt` — Actions table CRUD + compact
- `src/main/kotlin/.../codex/service/SyncService.kt` — SyncRepository facade
- `src/main/kotlin/.../codex/repository/SyncRepository.kt` — SyncEntries table (Exposed DAO)
- `src/main/kotlin/.../codex/sync/JvmSyncGenerators.kt` — SHA-256 checksum generator
- `src/main/resources/application.yaml` — Central configuration file
- `src/main/resources/db/migration/` — SQL migration files

---

## Published Documentation

The Codex documentation is published in the **DiCentra** collection under **AreaAssist › Codex**:

- [Codex Overview](https://docs.mallne.cloud/doc/codex-jg1U18vr8S) — Hub with subsystems table and quick start (📡)
  - [Architecture & Configuration](https://docs.mallne.cloud/doc/architecture-configuration-y09fYeV1uB) — Boot sequence, Koin DI, full config/env var reference, DB schema, Synapse relationship (🏗️)
  - [Self-Hosting Quickstart (Admin)](https://docs.mallne.cloud/doc/self-hosting-quickstart-admin-mTFbYz0LxN) — Step-by-step deployment: clone, configure, build, production JAR, OIDC setup, troubleshooting (🏁)
  - [Sync Protocol](https://docs.mallne.cloud/doc/sync-protocol-RGbku46Me3) — Differential sync: attestations, upload/download/delete, checksums (SHA-256), versioning, scope enforcement (🔄)
  - [Server-Side Actions — /bundle](https://docs.mallne.cloud/doc/server-side-actions-bundle-bqJqwDlyWN) — Deferred action storage: ActionDTO, CRUD, TTL lifecycle, scope isolation, compact strategy (⚡)
  - [Builtin Services & OIDC Auto-Configuration](https://docs.mallne.cloud/doc/builtin-services-oidc-auto-configuration-BzgkchVmAl) — Hardcoded service registry, OIDC OpenAPI generation, auto-ingest, Aviator locators (🔐)
- [AreaAssist Parent](https://docs.mallne.cloud/doc/areaassist-OdQaszP8JZ) — Top-level AreaAssist overview linking App, Codex, and Shared
