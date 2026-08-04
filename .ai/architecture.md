# Architecture -- AreaAssist Server (Codex)

## Purpose

AreaAssist Codex is the backend server for the AreaAssist ecosystem. It provides service discovery, server-side action storage, and differential sync for AreaAssist clients. Built on the Synapse framework.

## Tech Stack

- **Language:** Kotlin (JVM)
- **Framework:** Ktor (Netty engine)
- **DI:** Koin (KSP + manual `module {}` blocks)
- **Database:** PostgreSQL via Exposed ORM
- **Migrations:** Flyway-style (auto-applied on startup)
- **Serialization:** kotlinx.serialization
- **Security:** OIDC (optional, delegated to Synapse)
- **Core Libraries:** `dc.synapse.core`, `dc.areaassist.shared`

## Project Structure

```
areaassist/server/
+-- src/main/kotlin/.../codex/
|   +-- Application.kt       # Entry point, codexModule()
|   +-- di/
|   |   +-- DCAAAppModule.kt # Koin DI setup
|   +-- model/
|   |   +-- Config.kt        # AreaAssist-specific config
|   |   +-- ActionDTO.kt     # Server-side action data model
|   +-- routes/
|   |   +-- Builtin.kt       # /services/builtin, /services/builtin/ingest
|   |   +-- Health.kt        # /health
|   |   +-- StoredSearch.kt  # /bundle/* CRUD
|   |   +-- SyncRoutes.kt    # /sync, /sync/attestations
|   +-- service/
|   |   +-- ActionsService.kt
|   |   +-- SyncService.kt
|   +-- repository/
|   |   +-- SyncRepository.kt
|   +-- sync/
|       +-- JvmSyncGenerators.kt  # SHA-256 checksum
+-- src/main/resources/
    +-- application.yaml
    +-- db/migration/         # SQL migrations (V*__*.sql)
```

## API Routes

| Route | Purpose |
|-------|---------|
| `/services/builtin` | Hardcoded service registry |
| `/services/builtin/ingest` | Auto-ingest builtin services (SuperAdmin) |
| `/health` | Health check endpoint |
| `/bundle/*` | Server-side actions CRUD |
| `/sync` | Differential sync upload/download |
| `/sync/attestations` | Sync attestation exchange |
| Plus all Synapse routes | `/services`, `/catalyst`, `/scope`, `/discovery` |

## Database Tables

1. `apiservicedata` -- Service definitions
2. `scopes` -- Multi-tenant scope management
3. `actions` -- Server-side action storage
4. `sync_entries` -- Differential sync entries

## Dependencies on Other Modules

- **Synapse** (as library `dc.synapse.core`) -- Auth, DB, routing, Catalyst MCP
- **Shared** (`dc.areaassist.shared`) -- Parcel/sync data models, GIS adapters
- **Aviator** -- Service registration and OpenAPI handling

## Non-negotiable Rules

- Authentication is delegated to Synapse's `configureSecurity()` -- never implement custom auth
- `/services/builtin/ingest` requires SuperAdmin
- Migrations use `V<Version>__<Description>.sql` naming
- Use `DATA_AUTOCREATEDELTA` for delta migrations
