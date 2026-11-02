# AreaAssist Codex (Server)

**Stack**: Ktor/Netty on Synapse. Service discovery + sync + actions backend.

> **Full docs**: [areaassist/server/.ai/](.ai/) | [Notary](https://docs.mallne.cloud/doc/codex-jg1U18vr8S)

## Critical Rules

1. Authentication delegated to Synapse's `configureSecurity()` -- no custom auth
2. `/services/builtin/ingest` requires SuperAdmin
3. Sync uses differential sync with SHA-256 checksums
4. DB migrations use `V<Version>__<Description>.sql` naming
5. Actions are scope-filtered and have TTL lifecycle

## Build

```bash
./gradlew --no-daemon build
./gradlew --no-daemon run
docker compose up -d postgres  # Only if no PostgreSQL instance is already running
```

Run from this checkout with Java 25 and exported database credentials; Gradle does not
load `.env`. **Codex must be running for AreaAssist Web**, including static Docker hosting.
See [Web backend startup](.ai/commands.md#backend-required-for-areaassist-web) for port
8081, discovery, exact frontend CORS origins, and safe isolated-schema setup. Never
reset/repair the existing `codex` schema to work around migration/checksum failures.
