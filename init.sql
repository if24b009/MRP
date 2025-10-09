-- Enums
CREATE TYPE genre AS ENUM (
    'ACTION', 'ADVENTURE', 'COMEDY', 'DRAMA', 'THRILLER',
    'SCI_FI', 'HORROR', 'FANTASY', 'ROMANCE', 'DOCUMENTARY', 'ANIMATION'
);

CREATE TYPE media_entry_type AS ENUM (
    'MOVIE', 'SERIES', 'GAME'
);

-- Users
CREATE TABLE app_user (
    user_id VARCHAR(100) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hashed TEXT NOT NULL
);

-- Media Entries
CREATE TABLE media_entry (
    id VARCHAR(100) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    type media_entry_type,
    release_year INT,
    age_restriction INT,
    creator_id VARCHAR(100) REFERENCES app_user(user_id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MediaEntry <-> Genre (N:M)
CREATE TABLE media_entry_genre (
    media_entry_id VARCHAR(100) REFERENCES media_entry(id) ON DELETE CASCADE,
    genre genre NOT NULL,
    PRIMARY KEY (media_entry_id, genre)
);

-- Ratings
CREATE TABLE rating (
    id VARCHAR(100) PRIMARY KEY,
    user_id VARCHAR(100) REFERENCES app_user(user_id) ON DELETE CASCADE,
    media_entry_id VARCHAR(100) REFERENCES media_entry(id) ON DELETE CASCADE,
    stars_ct INT CHECK (stars_ct BETWEEN 1 AND 5),
    comment TEXT,
    is_comment_visible BOOLEAN DEFAULT FALSE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rating Likes (User <-> Rating N:M)
CREATE TABLE rating_likes (
    rating_id VARCHAR(100) REFERENCES rating(id) ON DELETE CASCADE,
    user_id VARCHAR(100) REFERENCES app_user(user_id) ON DELETE CASCADE,
    PRIMARY KEY (rating_id, user_id)
);

-- Tokens
CREATE TABLE token (
    token VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(100) REFERENCES app_user(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
