# Commands and Environment -- AreaAssist Server (Codex)

## Scripts

```bash
# Build the project
./gradlew build

# Run the server (development)
./gradlew run

# Create a fat JAR (Shadow JAR)
./gradlew shadowJar

# Run tests
./gradlew test

# Start dependencies (PostgreSQL)
docker-compose up -d
```

## Local Dev Setup

1. Ensure JDK 17+ is installed
2. Start PostgreSQL: `docker-compose up -d`
3. Run `./gradlew run` to start the server
4. Configuration is in `src/main/resources/application.yaml`

## Environment Variables

### Database

| Variable | Purpose | Default |
|----------|---------|---------|
| `DATA_URL` | JDBC Connection URL | **Required** |
| `DATA_USER` | Database username | **Required** |
| `DATA_PASSWORD` | Database password | **Required** |
| `DATA_SCHEMA` | Database schema | `codex` |
| `DATA_AUTOCREATEDELTA` | Auto-create delta migrations | `false` |

### Server

| Variable | Purpose | Default |
|----------|---------|---------|
| `TLSENABLED` | Enable TLS/HTTPS | `true` |
| `HOSTNAME` | Server hostname | (empty) |
| `CORS_ALL` | Allow all CORS origins | `false` |

### Catalyst (MCP/LLM)

| Variable | Purpose | Default |
|----------|---------|---------|
| `CATALYST_ENABLED` | Enable Catalyst integration | `true` |
| `CATALYST_ANONYMOUS` | Allow anonymous Catalyst requests | `true` |

### Security

| Variable | Purpose | Default |
|----------|---------|---------|
| `SECURITY_ENABLED` | Enable OIDC Authentication | `false` |
| `SECURITY_ISSUER` | OIDC Provider Issuer URL | (empty) |
| `SECURITY_CLIENTID` | OIDC Client ID | (empty) |
| `SECURITY_CLIENTSECRET` | OIDC Client Secret | (empty) |

## Runtime Notes

- PostgreSQL is required. Use `docker-compose up -d`.
- Shadow JAR is the production artifact.
- Migrations are auto-applied on startup.
