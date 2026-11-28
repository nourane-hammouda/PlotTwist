CREATE TABLE f4_declaration (
    id VARCHAR(32) PRIMARY KEY,
    seed VARCHAR(16) NOT NULL,
    suspect_id VARCHAR(32) NOT NULL,
    suspect_name VARCHAR(120) NOT NULL,
    place_id VARCHAR(32) NOT NULL,
    place_name VARCHAR(120) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    claims_alone BOOLEAN NOT NULL,
    CONSTRAINT uq_f4_declaration_seed_id UNIQUE (seed, id),
    CONSTRAINT ck_f4_declaration_interval CHECK (start_time < end_time)
);

CREATE INDEX idx_f4_declaration_seed_start
    ON f4_declaration(seed, start_time);

CREATE TABLE f4_ground_truth_lying_declaration (
    id VARCHAR(64) PRIMARY KEY,
    seed VARCHAR(16) NOT NULL,
    declaration_id VARCHAR(32) NOT NULL,
    CONSTRAINT uq_f4_ground_truth_seed_declaration UNIQUE (seed, declaration_id),
    CONSTRAINT fk_f4_ground_truth_declaration
        FOREIGN KEY (seed, declaration_id) REFERENCES f4_declaration(seed, id)
);

INSERT INTO f4_declaration
    (id, seed, suspect_id, suspect_name, place_id, place_name, start_time, end_time, claims_alone)
VALUES
    ('d1', '#AF42', 's1', 'Éloïse Varenne', 'r3', 'La loge 3', '21:00', '21:30', TRUE),
    ('d2', '#AF42', 's2', 'Gabriel Moreau', 'r3', 'La loge 3', '21:15', '21:25', FALSE),
    ('d3', '#AF42', 's4', 'Mina Sorel', 'r1', 'La scène', '21:40', '22:00', FALSE),
    ('d4', '#AF42', 's4', 'Mina Sorel', 'r4', 'La régie', '21:50', '22:10', FALSE),
    ('d5', '#AF42', 's5', 'Thomas Rivière', 'r1', 'La scène', '21:10', '21:20', FALSE),
    ('d6', '#AF42', 's3', 'Léonard Faye', 'r2', 'Les coulisses', '21:30', '21:40', FALSE);

INSERT INTO f4_ground_truth_lying_declaration (id, seed, declaration_id)
VALUES
    ('#AF42:d1', '#AF42', 'd1'),
    ('#AF42:d3', '#AF42', 'd3');
