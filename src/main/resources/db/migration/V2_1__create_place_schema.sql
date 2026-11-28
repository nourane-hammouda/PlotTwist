-- Migration F2 : Plan des lieux et topologie
-- Auteure : Chaïma Mabrouki

CREATE TABLE place (
    id      VARCHAR(100)  PRIMARY KEY,
    name    VARCHAR(255)  NOT NULL,
    floor   INT           NOT NULL DEFAULT 0,
    type    VARCHAR(100)  NOT NULL
);

CREATE TABLE doorway (
    place_a_id       VARCHAR(100)      NOT NULL REFERENCES place(id) ON DELETE CASCADE,
    place_b_id       VARCHAR(100)      NOT NULL REFERENCES place(id) ON DELETE CASCADE,
    distance_metres  DOUBLE PRECISION  NOT NULL CHECK (distance_metres > 0),
    PRIMARY KEY (place_a_id, place_b_id)
);

-- Données de démonstration : manoir victorien (7 pièces)
INSERT INTO place (id, name, floor, type) VALUES
    ('entree',      'Hall d''entrée',     0,  'circulation'),
    ('salon',       'Grand Salon',        0,  'reception'),
    ('cuisine',     'Cuisine',            0,  'service'),
    ('bibliotheque','Bibliothèque',       1,  'prive'),
    ('bureau',      'Bureau du maître',   1,  'prive'),
    ('cave',        'Cave à vin',        -1,  'stockage'),
    ('grenier',     'Grenier',            2,  'stockage');

INSERT INTO doorway (place_a_id, place_b_id, distance_metres) VALUES
    ('entree',       'salon',        4.5),
    ('entree',       'cuisine',      6.0),
    ('salon',        'bibliotheque', 12.0),
    ('salon',        'cave',         8.5),
    ('bibliotheque', 'bureau',       3.0),
    ('cuisine',      'cave',         5.0),
    ('bureau',       'grenier',      7.0);
