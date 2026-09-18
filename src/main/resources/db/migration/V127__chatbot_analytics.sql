CREATE TABLE chatbot_analytics_sessions (
    id UUID PRIMARY KEY,
    subject_key VARCHAR(64) NOT NULL,
    subject_key_version SMALLINT NOT NULL,
    environment VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL,
    is_test BOOLEAN NOT NULL DEFAULT FALSE,
    audience VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',

    started_at TIMESTAMPTZ NOT NULL,
    last_interaction_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    booking_started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,

    current_step VARCHAR(64),
    last_milestone VARCHAR(64),
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN (
            'ACTIVE', 'COMPLETED', 'ABANDONED', 'EXPIRED',
            'DECLINED', 'HANDED_OFF', 'WAITLISTED'
        )),
    end_reason VARCHAR(64),
    booking_group_code VARCHAR(40),
    reservation_id UUID,

    CHECK (last_interaction_at >= started_at),
    CHECK (expires_at >= last_interaction_at),
    CHECK ((status = 'ACTIVE') = (ended_at IS NULL)),
    CHECK ((status = 'COMPLETED') = (completed_at IS NOT NULL))
);

CREATE UNIQUE INDEX uq_chatbot_active_subject
    ON chatbot_analytics_sessions (
        environment, source, subject_key_version, subject_key
    )
    WHERE status = 'ACTIVE';

CREATE INDEX idx_chatbot_sessions_started
    ON chatbot_analytics_sessions(started_at);

CREATE INDEX idx_chatbot_sessions_expiry
    ON chatbot_analytics_sessions(expires_at)
    WHERE status = 'ACTIVE';

CREATE TABLE chatbot_analytics_events (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL
        REFERENCES chatbot_analytics_sessions(id) ON DELETE CASCADE,
    event_type VARCHAR(64) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    step VARCHAR(64),
    reason_code VARCHAR(64),
    dedupe_key VARCHAR(128) NOT NULL,
    UNIQUE (session_id, dedupe_key)
);

CREATE INDEX idx_chatbot_events_session_time
    ON chatbot_analytics_events(session_id, occurred_at);
