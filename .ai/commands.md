# Commands and Environment -- AreaAssist Server (Codex)

## Scripts

```bash
# Build the project
./gradlew --no-daemon build

# Run the server (development)
./gradlew --no-daemon run

# Create a fat JAR (Shadow JAR)
./gradlew --no-daemon shadowJar

# Run tests
./gradlew --no-daemon test

# Start dependencies (PostgreSQL)
docker compose up -d postgres
```

## Local Dev Setup

Run all commands from this Codex checkout's root. Java 25 is required (`java -version`).
No parent/sibling checkout is required: absent optional composite builds, dependencies
resolve from the configured artifact registry. Export `NEXUS_USERNAME` and
`NEXUS_PASSWORD` when registry authentication is required. In the monorepo the pinned
root Nix devShell can supply Java 25; do not change its pins for startup failures.

Configuration is in `src/main/resources/application.conf` (HOCON), with standard defaults
followed by `${?ENV}` overrides. `DATA_URL`, `DATA_USER`, and `DATA_PASSWORD` remain required
substitutions without defaults; Codex no longer depends on `ktor-server-config-yaml`.
Export database credentials
through a trusted shell environment or secret manager; **Gradle does not load `.env`**.
Do not blindly source `.env.example` (it contains placeholders and bare optional keys).
`DATA_URL` must look like `postgresql://localhost:5432/postgres`, without `jdbc:` or
`r2dbc:` prefixes. For an interactive Bash shell, credentials can be read without putting
the password in shell history:

```bash
export DATA_URL=postgresql://localhost:5432/postgres
read -r -p 'Database user: ' DATA_USER; export DATA_USER
read -r -s -p 'Database password: ' DATA_PASSWORD; echo; export DATA_PASSWORD
```

Use an existing PostgreSQL instance, or, after checking port 5432 is free, run
`docker compose up -d postgres`. Compose uses the exported `DATA_USER`/`DATA_PASSWORD`
for initial provisioning; an existing volume retains its original credentials. Do not
remove volumes or replace a running database to resolve a startup problem.

### HOCON migration verification

At the user's explicit request, the introduced `HoconConfigurationTest`, direct
`testImplementation(libs.typesafe.config)`, and catalog alias were removed. Production
HOCON migration is unchanged. The earlier successful run (3 HOCON configuration tests
under pinned Nix Java 25) is historical only, before these removals; no post-removal
run is recorded here.

### Backend required for AreaAssist Web

Start Codex **before the Web frontend** and keep it running (also for a Docker-hosted
static frontend). Local example, after exporting database credentials:

```bash
PORT=8081 HOSTNAME=localhost:8081 TLSENABLED=false \
CATALYST_SERVERNAME=localhost:8081 CATALYST_TLSENABLED=false \
CATALYST_ENABLED=true CATALYST_ANONYMOUS=true \
CORS_ALL=false CORS_HOSTS=localhost:8080,localhost:8090 \
DATA_SCHEMA=codex_web_dev DATA_AUTOCREATEDELTA=false \
./gradlew --no-daemon run
```

Use an approved isolated development schema; `codex_web_dev` is an example, not a reset
instruction. The DB user needs schema creation/migration rights. Startup applies migrations;
the existing `codex` schema has a known migration/checksum mismatch. **Never reset, clean,
repair or rewrite its migration history for Web startup.** Investigate failures or select
an approved separate schema without modifying existing data.

In a second terminal:

```bash
curl --fail http://localhost:8081/health  # RUNNING
curl --fail -i -H 'Origin: http://localhost:8080' \
  'http://localhost:8081/services?transformationType=Catalyst'
```

Expect HTTP 200 and `Access-Control-Allow-Origin: http://localhost:8080`; repeat with
Origin `http://localhost:8090` for the Docker frontend. `CORS_HOSTS` contains frontend
host:port values (no scheme/path), not the backend port. The Synapse dependency must
support comma-separated scalar `CORS_HOSTS`; if the header is missing, verify the resolved
artifact/configuration instead of enabling wildcard CORS.

The browser consumes HTTP discovery at `/services?transformationType=Catalyst`, then uses
Codex's Catalyst gateway for provider requests. In the app checkout, set
`discovery=http://localhost:8081` in ignored `local.properties`, preserving other keys,
then rebuild/run `./gradlew --no-daemon :composeApp:jsBrowserDevelopmentRun` for port 8080.
Discovery is compiled into the frontend; no runtime Docker environment override exists.
Use the base URL, not `/discovery` or `/services`. For remote browsers, use a reachable
HTTPS backend and actual frontend origins, not browser-local `localhost`.

A fresh schema has no published providers. Publication through
`/services/builtin/ingest` requires SuperAdmin; do not bypass authorization or seed tables
directly. Catalyst outbound access additionally requires exact
`CATALYST_ALLOWED_HOSTS` (e.g. `www.geoproxy.geoportal-th.de`) or explicitly approved
`CATALYST_ALLOW_PUBLIC_SERVICE_HOSTS=true` for curated public specs; scoped providers
still require an allowlist. Leave `PREFERRED_TRANSFORM` unchanged: Web explicitly requests
Catalyst. Health/discovery checks are not proof of working provider access.

Keep the server in the foreground and stop it with Ctrl-C; avoid broad JVM/process kills.
If a composite build exhausts memory, use process-only Gradle JVM settings such as
`'-Dorg.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m'` before `run`; do not change
toolchain pins or commit machine-specific memory settings.

## Environment Variables

### Database

| Variable | Purpose | Default |
|----------|---------|---------|
| `DATA_URL` | PostgreSQL URL without driver prefix (`postgresql://host:5432/database`) | **Required** |
| `DATA_USER` | Database username | **Required** |
| `DATA_PASSWORD` | Database password | **Required** |
| `DATA_SCHEMA` | Database schema | `codex` |
| `DATA_AUTOCREATEDELTA` | Auto-create delta migrations | `false` |

### Server

| Variable | Purpose | Default |
|----------|---------|---------|
| `TLSENABLED` | Enable TLS/HTTPS | `true` |
| `PORT` | HTTP listen port | `8080` |
| `HOSTNAME` | Server hostname | (empty) |
| `CORS_ALL` | Allow all CORS origins | `false` |
| `CORS_HOSTS` | Comma-separated frontend host[:port] allowlist | (empty) |

### Catalyst (MCP/LLM)

| Variable | Purpose | Default |
|----------|---------|---------|
| `CATALYST_ENABLED` | Enable Catalyst integration | `true` |
| `CATALYST_ANONYMOUS` | Allow anonymous Catalyst requests | `true` |
| `CATALYST_SERVERNAME` | Host[:port] advertised for gateway requests | (empty) |
| `CATALYST_TLSENABLED` | Advertise HTTPS gateway URLs | `true` |
| `CATALYST_ALLOWED_HOSTS` | Exact outbound provider hosts | (empty) |
| `CATALYST_ALLOW_PUBLIC_SERVICE_HOSTS` | Trust exact hosts from curated public specs | `false` |

### Security

| Variable | Purpose | Default |
|----------|---------|---------|
| `SECURITY_ENABLED` | Enable OIDC Authentication | `false` |
| `SECURITY_ISSUER` | OIDC Provider Issuer URL | (empty) |
| `SECURITY_CLIENTID` | OIDC Client ID | (empty) |
| `SECURITY_CLIENTSECRET` | OIDC Client Secret | (empty) |

## Runtime Notes

- PostgreSQL is required. Use an existing instance or `docker compose up -d postgres`.
- Shadow JAR is the production artifact.
- Migrations are auto-applied on startup.
