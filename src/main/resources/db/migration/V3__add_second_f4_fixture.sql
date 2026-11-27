INSERT INTO f4_declaration
    (id, seed, suspect_id, suspect_name, place_id, place_name, start_time, end_time, claims_alone)
VALUES
    ('b1', '#B731', 's2', 'Gabriel Moreau', 'r2', 'Les coulisses', '20:05', '20:30', FALSE),
    ('b2', '#B731', 's2', 'Gabriel Moreau', 'r4', 'La régie', '20:20', '20:40', FALSE),
    ('b3', '#B731', 's3', 'Léonard Faye', 'r1', 'La scène', '20:40', '21:10', TRUE),
    ('b4', '#B731', 's1', 'Éloïse Varenne', 'r1', 'La scène', '20:50', '21:00', FALSE),
    ('b5', '#B731', 's4', 'Mina Sorel', 'r3', 'La loge 3', '21:00', '21:20', FALSE),
    ('b6', '#B731', 's5', 'Thomas Rivière', 'r4', 'La régie', '21:10', '21:30', FALSE);

INSERT INTO f4_ground_truth_lying_declaration (id, seed, declaration_id)
VALUES
    ('#B731:b1', '#B731', 'b1'),
    ('#B731:b3', '#B731', 'b3');
