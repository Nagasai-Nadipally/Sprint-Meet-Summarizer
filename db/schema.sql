-- =============================================================================
-- Reference schema for the AI Meeting Notes app.
--
-- This file documents the table design. You do NOT need to run it: the app uses
-- Hibernate with `ddl-auto: update`, so the schema is created automatically on
-- first start. For production, switch ddl-auto to `validate` and manage schema
-- changes with a migration tool (Flyway/Liquibase) using files like this one.
-- =============================================================================

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    full_name     VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at    TIMESTAMPTZ  NOT NULL
);

CREATE TABLE meetings (
    id                BIGSERIAL PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_key       VARCHAR(255) NOT NULL,
    file_size_bytes   BIGINT,
    status            VARCHAR(20)  NOT NULL DEFAULT 'UPLOADED',
    error_message     VARCHAR(1000),
    user_id           BIGINT       NOT NULL REFERENCES users (id),
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ
);
CREATE INDEX idx_meetings_user ON meetings (user_id, created_at DESC);

CREATE TABLE transcripts (
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT      NOT NULL UNIQUE REFERENCES meetings (id) ON DELETE CASCADE,
    content    TEXT        NOT NULL,
    language   VARCHAR(16),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE summaries (
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT      NOT NULL UNIQUE REFERENCES meetings (id) ON DELETE CASCADE,
    overview   TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

-- Key points and follow-up questions are @ElementCollection child tables.
CREATE TABLE summary_key_points (
    summary_id BIGINT        NOT NULL REFERENCES summaries (id) ON DELETE CASCADE,
    point      VARCHAR(1000)
);

CREATE TABLE summary_follow_up_questions (
    summary_id BIGINT        NOT NULL REFERENCES summaries (id) ON DELETE CASCADE,
    question   VARCHAR(1000)
);

CREATE TABLE action_items (
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT       NOT NULL REFERENCES meetings (id) ON DELETE CASCADE,
    owner      VARCHAR(255) NOT NULL,
    task       VARCHAR(2000) NOT NULL,
    deadline   DATE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_action_items_meeting ON action_items (meeting_id);
