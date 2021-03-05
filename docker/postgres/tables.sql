CREATE EXTENSION pgcrypto;

CREATE TABLE users
(
    id       BIGINT PRIMARY KEY,
    email    TEXT   NOT NULL,
    password TEXT   NOT NULL,
    username TEXT   NOT NULL,
    gender   CHAR            DEFAULT NULL,
    birthday DATE            DEFAULT NULL,
    summary  TEXT            DEFAULT NULL,
    avatar   TEXT            DEFAULT NULL,
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
    language VARCHAR(2) REFERENCES languages (code),
    PRIMARY KEY (id, language)
);

CREATE TABLE posts
(
    id      BIGINT PRIMARY KEY,
    author  BIGINT REFERENCES users (id),
    content TEXT   NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE likes
(
    post  BIGINT REFERENCES posts (id),
    liker BIGINT REFERENCES users (id),
    PRIMARY KEY (post, liker)
);

CREATE TABLE comments
(
    id        BIGINT PRIMARY KEY,
    post      BIGINT REFERENCES posts (id),
    commenter BIGINT REFERENCES users (id),
    content   TEXT   NOT NULL,
    version   BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE follows
(
    follower BIGINT REFERENCES users (id),
    followee BIGINT REFERENCES users (id)
);
