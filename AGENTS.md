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
./gradlew build
./gradlew run
docker-compose up -d  # PostgreSQL required
```
