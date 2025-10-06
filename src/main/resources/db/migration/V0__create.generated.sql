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
    id       SERIAL PRIMARY KEY,
    "name"   VARCHAR(255)                        NOT NULL,
    attaches VARCHAR(255)                        NOT NULL,
    created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS actions
(
    "action" JSONB                                                                    NOT NULL,
    scope    VARCHAR(255)                                                             NULL,
    created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP                                      NOT NULL,
    expires  TIMESTAMP DEFAULT '2025-10-20 12:41:58.592'::timestamp without time zone NOT NULL,
    id       VARCHAR(36)                                                              NOT NULL
);
CREATE SEQUENCE IF NOT EXISTS Scopes_id_seq START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775807;
