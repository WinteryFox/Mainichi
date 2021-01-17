CREATE TABLE users
(
    id       BIGINT PRIMARY KEY,           -- The ID of the user
    email    TEXT   NOT NULL,              -- The user's email
    username TEXT   NOT NULL,              -- The username of the user
    gender   CHAR            DEFAULT NULL, -- The gender of the user (female, male, unknown)
    birthday DATE            DEFAULT NULL, -- The user's birthday
    summary  TEXT            DEFAULT NULL, -- The summary/description/self-introduction of the user
    avatar   TEXT            DEFAULT NULL, -- The user's avatar hash
    version  BIGINT NOT NULL DEFAULT 0,
    CHECK (gender IN ('F', 'M'))
);

CREATE TABLE learning
(
    id          BIGINT REFERENCES users (id),
    language    VARCHAR(2) REFERENCES languages (code),
    proficiency SMALLINT NOT NULL,
    PRIMARY KEY (id, language)
);

CREATE TABLE proficient
(
    id       BIGINT REFERENCES users (id),
    language VARCHAR(5) REFERENCES languages (code),
    PRIMARY KEY (id, language)
);

CREATE TABLE posts
(
    id      BIGINT PRIMARY KEY,           -- The ID of the post
    author  BIGINT REFERENCES users (id), -- The author of the post
    content TEXT   NOT NULL,              -- The content of the post
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE likes
(
    post  BIGINT REFERENCES posts (id), -- The post liked
    liker BIGINT REFERENCES users (id), -- The user who liked the post
    PRIMARY KEY (post, liker)
);

CREATE TABLE comments
(
    id        BIGINT PRIMARY KEY,           -- The comment's ID
    post      BIGINT REFERENCES posts (id), -- The post the comment is on
    commenter BIGINT REFERENCES users (id), -- User the comment is from
    content   TEXT   NOT NULL,              -- The content of the comment
    version   BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE follows
(
    follower BIGINT REFERENCES users (id), -- The person following
    followee BIGINT REFERENCES users (id)  -- The person being followed
);

CREATE TABLE sessions
(
    id               UUID PRIMARY KEY,
    attributes       BYTEA     NOT NULL,
    max_idle_time    BIGINT    NOT NULL,
    creation_time    TIMESTAMP NOT NULL,
    last_access_time TIMESTAMP NOT NULL,
    valid            BOOLEAN   NOT NULL,
    version          BIGINT    NOT NULL
)
