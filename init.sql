-- Enums
CREATE TYPE genre AS ENUM (
    'ACTION', 'ADVENTURE', 'COMEDY', 'DRAMA', 'THRILLER',
    'SCI_FI', 'HORROR', 'FANTASY', 'ROMANCE', 'DOCUMENTARY', 'ANIMATION'
);

CREATE TYPE media_entry_type AS ENUM (
    'MOVIE', 'SERIES', 'GAME'
);

-- Users
CREATE TABLE app_user
(
    user_id         UUID PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hashed TEXT         NOT NULL
);

-- Media Entries
CREATE TABLE media_entry
(
    id              UUID PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    type            media_entry_type,
    release_year    INT,
    age_restriction INT,
    creator_id      UUID         REFERENCES app_user (user_id) ON DELETE SET NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MediaEntry <-> Genre (N:M)
CREATE TABLE media_entry_genre
(
    media_entry_id UUID REFERENCES media_entry (id) ON DELETE CASCADE,
    genre          genre NOT NULL,
    PRIMARY KEY (media_entry_id, genre)
);

-- Ratings
CREATE TABLE rating
(
    id                 UUID PRIMARY KEY,
    user_id            UUID REFERENCES app_user (user_id) ON DELETE CASCADE,
    media_entry_id     UUID REFERENCES media_entry (id) ON DELETE CASCADE,
    stars_ct           INT CHECK (stars_ct BETWEEN 1 AND 5),
    comment            TEXT,
    is_comment_visible BOOLEAN   DEFAULT FALSE,
    timestamp          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rating Likes (User <-> Rating N:M)
CREATE TABLE rating_likes
(
    rating_id UUID REFERENCES rating (id) ON DELETE CASCADE,
    user_id   UUID REFERENCES app_user (user_id) ON DELETE CASCADE,
    PRIMARY KEY (rating_id, user_id)
);

-- Favorites
CREATE TABLE favorite
(
    id             UUID PRIMARY KEY,
    user_id        UUID REFERENCES app_user (user_id) ON DELETE CASCADE,
    media_entry_id UUID REFERENCES media_entry (id) ON DELETE CASCADE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, media_entry_id) -- prevent duplicates
);

-- Tokens
CREATE TABLE token
(
    token      VARCHAR(255) PRIMARY KEY,
    user_id    UUID REFERENCES app_user (user_id) ON DELETE CASCADE UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
