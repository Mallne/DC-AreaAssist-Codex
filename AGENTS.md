# DiCentra AreaAssist Codex (Synapse)

## Project Overview

**AreaAssist Codex** (part of the **Synapse** framework) is a high-performance discovery and service management server
built with **Kotlin** and **Ktor**. It serves as a central registry for services within the DiCentra Application
Framework, enabling dynamic service discovery, scope-based access control, and seamless integration with LLM agents via
the **Catalyst** (MCP) protocol.

### Main Technologies

- **Language:** Kotlin (JVM)
- **Framework:** Ktor (Netty engine)
- **Dependency Injection:** Koin (with KSP-generated modules)
- **Persistence:** PostgreSQL (with automated migrations)
- **Serialization:** Kotlin Serialization (JSON)
- **Security:** OIDC-based authentication (optional/configurable)
- **Core Libraries:** `dc.synapse.core`, `dc.areaassist.shared`

### Architecture

- **Routing:** Organized into modular route files (`Builtin.kt`, `StoredSearch.kt`, `Health.kt`). Uses
  `DiscoveryGenerator` for self-documenting API endpoints.
- **Services:** Logic is encapsulated in services (e.g., `ActionsService`, `APIDBService`) injected via Koin.
- **Data Model:** Centered around `APIServiceDTO`, `User` (with roles), and `Scope`.
- **Catalyst:** Provides a specialized interface (`/catalyst`) for aggregating and executing requests across discovered
  services, optimized for LLM agents.

---

## Building and Running

### Prerequisites

- JDK 17+
- Docker (for PostgreSQL)

### Key Commands

- **Build the project:**
  ```bash
  ./gradlew build
  ```
- **Run the server (Development):**
  ```bash
  ./gradlew run
  ```
- **Create a fat JAR (Shadow JAR):**
  ```bash
  ./gradlew shadowJar
  ```
- **Run Tests:**
  ```bash
  ./gradlew test
  ```
- **Start Dependencies (Postgres):**
  ```bash
  docker-compose up -d
  ```

---

## Development Conventions

### Coding Style

- Follows standard Kotlin idiomatic patterns.
- Uses **Koin** for all dependency injection; ensure `DCAAAppModule` is updated when adding new services.
- **Routing:** New endpoints should be added to the `codexModule` and preferably registered with `discoveryGenerator`
  for documentation.

### Security & Authentication

- Authentication is handled via Ktor's `authenticate` blocks.
- Authorization levels: `user`, `admin`, `superadmin`.
- Scope-based filtering is enforced for service access.

### Database Migrations

- Migrations are located in `src/main/main/resources/db/migration`.
- Follow the `V<Version>__<Description>.sql` naming convention.
- Migrations are automatically applied on startup via the `configureDatabase` hook.

### API Response Structure

- Standardized responses often use the `@ResponseObject` annotation and `DiscoveryResponse` wrapper to include metadata
  about the system state and versioning.

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
| `CORS_ALL`           | Allow all CORS origins                | `false`                       |
| `CORS_HOSTS`         | Specific CORS hosts (comma-separated) | (empty)                       |

### Catalyst (MCP/LLM) Configuration (`catalyst:`)

| Variable             | Description                       | Default            |
|----------------------|-----------------------------------|--------------------|
| `CATALYST_ENABLED`   | Enable Catalyst integration       | `true`             |
| `CATALYST_ANONYMOUS` | Allow anonymous Catalyst requests | `true`             |
| `CATALYST_TITLE`     | Catalyst service title            | `Synapse Catalyst` |

### Security Configuration (`security:`)

| Variable                     | Description                   | Default |
|------------------------------|-------------------------------|---------|
| `SECURITY_ENABLED`           | Enable OIDC Authentication    | `false` |
| `SECURITY_ISSUER`            | OIDC Provider Issuer URL      | (empty) |
| `SECURITY_CLIENTID`          | OIDC Client ID                | (empty) |
| `SECURITY_CLIENTSECRET`      | OIDC Client Secret            | (empty) |
| `SECURITY_GROUPS_USER`       | OIDC group for standard users | (empty) |
| `SECURITY_GROUPS_ADMIN`      | OIDC group for admins         | (empty) |
| `SECURITY_GROUPS_SUPERADMIN` | OIDC group for superadmins    | (empty) |

---

## Key Files & Directories

- `src/main/kotlin/cloud/mallne/dicentra/areaassist/codex/Application.kt`: Main entry point and Koin module
  initialization.
- `src/main/kotlin/cloud/mallne/dicentra/areaassist/codex/routes/`: Contains all API endpoint definitions.
- `src/main/resources/application.yaml`: Central configuration file for server, database, and security.
- `src/main/resources/db/migration/`: SQL migration files for schema management.
- `build.gradle.kts`: Gradle build configuration and dependency management.
