# Business Rules -- AreaAssist Server (Codex)

Rules an agent must respect when writing code for this module.

## Sync Protocol

- **Rule**: Sync uses differential sync with SHA-256 checksums for data integrity.
- **Why**: Ensures reliable data synchronization between client and server with conflict detection.
- **Rule**: Sync entries are scope-filtered. Users can only sync data within their scope.
- **Why**: Multi-tenancy data isolation must be maintained at the sync level.

## Server-Side Actions

- **Rule**: Actions are stored via `ActionsService` and scoped to the creating user's scope.
- **Why**: Actions are tenant-specific and must not leak across scopes.
- **Rule**: Actions have TTL lifecycle and compaction strategy.
- **Why**: Prevents unbounded storage growth.

## Security

- **Rule**: Authentication is handled via Synapse's `configureSecurity()`. Never implement custom auth.
- **Why**: Consistent OIDC/Bearer token handling across all endpoints.
- **Rule**: Authorization levels: `user`, `admin`, `superadmin`. Scope-based filtering is mandatory.
- **Why**: Role-based access control prevents unauthorized operations.

## Database Migrations

- **Rule**: Follow `V<Version>__<Description>.sql` naming. Auto-applied on startup.
- **Why**: Flyway requires strict naming. The initial migration (`V0__create.generated.sql`) is auto-generated.

## API Response Structure

- **Rule**: Synapse-base routes use `@ResponseObject` and `DiscoveryResponse` wrapper.
- **Rule**: Codex-specific routes use plain types (`ActionDTO`, `SyncAggregateResponse`, etc.).
- **Why**: Consistent response format per route origin.

## Edge Cases

- **Standalone build**: The standalone GitHub repo cannot build in isolation -- requires sibling module artifacts.
- **Auto-created migrations**: Use `DATA_AUTOCREATEDELTA` for schema deltas; the initial migration is auto-generated.

## Overrides

- None. Codex follows root coding standards and inherits Synapse conventions.
