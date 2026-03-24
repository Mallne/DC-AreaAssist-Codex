CREATE TABLE IF NOT EXISTS apiservicedata
(
    service                JSONB                               NOT NULL,
    scope                  VARCHAR(255)                        NULL,
    created                TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    native_transformable   BOOLEAN   DEFAULT TRUE              NOT NULL,
    catalyst_transformable BOOLEAN   DEFAULT TRUE              NOT NULL,
    mcp_enabled            BOOLEAN   DEFAULT TRUE              NOT NULL,
    builtin                BOOLEAN   DEFAULT FALSE             NOT NULL,
    preferred_transform    INT       DEFAULT 0                 NOT NULL,
    id                     VARCHAR(36)                         NOT NULL
);
CREATE TABLE IF NOT EXISTS scopes
(
    id           SERIAL PRIMARY KEY,
    "scope_name" VARCHAR(255)                        NOT NULL,
    user_id      VARCHAR(255)                        NOT NULL,
    "role"       VARCHAR(20)                         NOT NULL,
    created      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
ALTER TABLE scopes
    ADD CONSTRAINT scopes_scope_name_user_id_unique UNIQUE ("scope_name", user_id);
CREATE TABLE IF NOT EXISTS actions
(
    "action" JSONB                                                    NOT NULL,
    scope    VARCHAR(255)                                             NULL,
    created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP                      NOT NULL,
    expires  TIMESTAMP DEFAULT CURRENT_TIMESTAMP + INTERVAL '14 days' NOT NULL,
    id       VARCHAR(36)                                              NOT NULL
);
CREATE SEQUENCE IF NOT EXISTS Scopes_id_seq START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775807;
