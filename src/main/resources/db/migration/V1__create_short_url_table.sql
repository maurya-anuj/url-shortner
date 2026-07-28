-- Initial schema: short_url table with unique code and dedup hash index
CREATE TABLE short_url (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(16) NOT NULL,
    long_url    TEXT NOT NULL,
    url_hash    CHAR(64) NOT NULL,
    is_custom   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_code ON short_url(code);
CREATE UNIQUE INDEX ux_url_hash ON short_url(url_hash) WHERE is_custom = FALSE;
CREATE SEQUENCE short_url_code_seq START 10000;
