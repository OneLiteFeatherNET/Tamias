CREATE TABLE tamias_player
(
    player_uuid UUID PRIMARY KEY,
    last_name   VARCHAR(16)              NOT NULL,
    name_key    VARCHAR(16)              NOT NULL,
    last_seen   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_tamias_player_name_key ON tamias_player (name_key);

CREATE TABLE tamias_round
(
    round_id     UUID PRIMARY KEY,
    game_id      UUID                     NOT NULL,
    round_number SMALLINT                 NOT NULL,
    map_name     VARCHAR(64)              NOT NULL,
    winner       VARCHAR(16)              NOT NULL,
    started_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_tamias_round_game ON tamias_round (game_id);

CREATE TABLE tamias_round_stats
(
    round_id    UUID        NOT NULL REFERENCES tamias_round (round_id) ON DELETE CASCADE,
    player_uuid UUID        NOT NULL,
    start_role  VARCHAR(16) NOT NULL,
    won         BOOLEAN     NOT NULL,
    converted   BOOLEAN     NOT NULL,
    kills       INT         NOT NULL,
    hits        INT         NOT NULL,
    PRIMARY KEY (round_id, player_uuid)
);

CREATE INDEX idx_tamias_round_stats_player ON tamias_round_stats (player_uuid);
