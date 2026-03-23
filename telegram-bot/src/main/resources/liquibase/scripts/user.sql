--liquibase formatted sql

-- changeset evs: 1

CREATE TABLE tasks_for_remind (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT,
    remind_text TEXT,
    date_time TIMESTAMP
)