# `.ai/` -- Agent Documentation for AreaAssist Server (Codex)

This folder is the AI-agent onboarding guide for the Codex submodule.

## Prerequisites

Read these root `.ai/` articles first:

- `../../.ai/coding-standards.md` -- shared lint/format conventions
- `../../.ai/testing-guidelines.md` -- shared test strategy
- `../../.ai/security-guidelines.md` -- shared auth/secrets patterns
- `../../.ai/glossary.md` -- shared domain terms (see Codex, Sync, Scope)

## Index

| Article | Contents |
|---------|----------|
| [architecture.md](architecture.md) | Server structure, routes, DB schema, Synapse integration |
| [business-rules.md](business-rules.md) | Sync protocol, actions, security rules |
| [commands.md](commands.md) | Scripts, env vars, local dev setup |

## Published Documentation

- [Codex Hub](https://docs.mallne.cloud/doc/codex-jg1U18vr8S)
- [Architecture and Configuration](https://docs.mallne.cloud/doc/architecture-configuration-y09fYeV1uB)
- [Self-Hosting Quickstart](https://docs.mallne.cloud/doc/self-hosting-quickstart-admin-mTFbYz0LxN)
- [Sync Protocol](https://docs.mallne.cloud/doc/sync-protocol-RGbku46Me3)
- [Server-Side Actions](https://docs.mallne.cloud/doc/server-side-actions-bundle-bqJqwDlyWN)
- [Builtin Services and OIDC](https://docs.mallne.cloud/doc/builtin-services-oidc-auto-configuration-BzgkchVmAl)

## Maintenance

- Update the relevant article on **every** edit that touches architecture, structure, commands, or rules.
- Whenever you discover an **inconsistency** between the docs and the code, fix it here.
- Do not duplicate content that lives in `../../.ai/` -- reference it instead.
