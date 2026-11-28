CREATE TABLE suspect_profile (
    id UUID PRIMARY KEY,
    seed VARCHAR(16) NOT NULL,
    name VARCHAR(120) NOT NULL,
    role VARCHAR(120) NOT NULL,
    biography VARCHAR(1000) NOT NULL
);

CREATE TABLE subjective_event (
    id UUID PRIMARY KEY,
    seed VARCHAR(16) NOT NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(2000),
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    subject_type VARCHAR(32) NOT NULL,
    declared_by UUID NOT NULL,
    CONSTRAINT fk_subjective_event_profile
        FOREIGN KEY (declared_by) REFERENCES suspect_profile(id)
);

CREATE TABLE subjective_event_participant (
    event_id UUID NOT NULL,
    suspect_id UUID NOT NULL,
    PRIMARY KEY (event_id, suspect_id),
    CONSTRAINT fk_event_participant_event
        FOREIGN KEY (event_id) REFERENCES subjective_event(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_participant_suspect
        FOREIGN KEY (suspect_id) REFERENCES suspect_profile(id)
);

CREATE INDEX idx_subjective_event_declared_by ON subjective_event(declared_by);
CREATE INDEX idx_subjective_event_occurred_at ON subjective_event(occurred_at);
