# DiCentra AreaAssist Codex

[![DiCentra](https://img.shields.io/badge/DiCentra-grey.svg)](https://code.mallne.cloud)
[![Kotlin](https://img.shields.io/badge/kotlin-grey.svg?logo=kotlin)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**AreaAssist Codex** is the central discovery and service management hub for the DiCentra Application Framework. Built
on the **Synapse** core, it provides a high-performance, secure registry for managing and discovering services across
the ecosystem.

---

## What it Does

Codex acts as the "Source of Truth" for all services within an AreaAssist environment. Its primary responsibilities
include:

- **Service Registry:** A centralized database (PostgreSQL) storing service definitions, locators, and metadata.
- **Dynamic Discovery:** Smart endpoints that filter available services based on user authentication, roles (`admin`,
  `superadmin`), and assigned scopes.
- **Catalyst Integration:** A specialized interface designed for LLM agents. It supports:
    - **Aggregation:** Sending requests to multiple service locators simultaneously.
    - **MCP Server:** Native support for the Model Context Protocol (MCP), allowing AI agents to interact with the
      service registry directly.
- **Self-Documenting API:** Automatically generates discovery responses that tell clients exactly what endpoints and
  operations are available.

---

## Prerequisites

- **JDK 17 or higher**
- **Docker & Docker Compose** (for the PostgreSQL database)

---

## Quick Start

### 1. Start the Database

Codex requires a PostgreSQL instance. Use the provided `docker-compose.yml` to spin it up:

```bash
docker-compose up -d
```

### 2. Configure the Environment

The server uses environment variables for configuration. You can set these in your shell or via a `.env` file.

**Essential Variables:**

- `DATA_URL`: JDBC URL (e.g., `jdbc:postgresql://localhost:5432/codex`)
- `DATA_USER` / `DATA_PASSWORD`: Database credentials.
- `SECURITY_ENABLED`: Set to `true` to enable OIDC authentication.
- `SECURITY_ISSUER`: Your OIDC provider URL (if security is enabled).

**Advanced Variables:**

- `CATALYST_ENABLED`: Toggle the LLM/MCP interface (default: `true`).
- `CORS_ALL`: Set to `true` for development if you encounter CORS issues.
- `AUTORELEASEVERSION`: Toggle auto-versioning in discovery responses.

See `GEMINI.md` for a full list of available variables.

### 3. Run the Server

Use the Gradle wrapper to start the server in development mode:

```bash
./gradlew run
```

The server will be available at `http://localhost:8080`.

---

## Key Endpoints

- **`GET /services`**: Retrieve all registered services (requires Admin/SuperAdmin).
- **`GET /services/builtin`**: List of hardcoded system services.
- **`GET /catalyst/mcp`**: The MCP (Model Context Protocol) entry point for LLM agents.
- **`GET /health`**: System health and database connectivity check.

---

## Building for Production

To create a standalone executable JAR containing all dependencies:

```bash
./gradlew shadowJar
```

The resulting JAR will be located in `build/libs/`.

---

## Development & Contribution

- **Architecture:** Ktor with Koin for Dependency Injection.
- **Migrations:** Managed via SQL scripts in `src/main/resources/db/migration/`.
- **Testing:** Run `./gradlew test` to execute the test suite.

---
<p align="center">
  Built with ❤️ by Mallne under the DiCentra umbrella
</p>