CREATE EXTENSION snowflake;

CREATE DOMAIN GENDER CHAR(1) CHECK (value IN ('F', 'M'));

CREATE TABLE users
(
    snowflake BIGINT PRIMARY KEY DEFAULT next_snowflake(0, TIMESTAMP '2020-01-01 00:00:00'), -- The ID of the user
    email     TEXT NOT NULL,                                                                 -- The user's email
    username  TEXT NOT NULL,                                                                 -- The username of the user
    gender    GENDER             DEFAULT NULL,                                               -- The gender of the user (female, male, unknown)
    birthday  DATE               DEFAULT NULL,                                               -- The user's birthday
    summary   TEXT               DEFAULT NULL,                                               -- The summary/description/self-introduction of the user
    avatar    TEXT               DEFAULT NULL                                                -- The user's avatar hash
);

CREATE TABLE learning
(
    snowflake   BIGINT REFERENCES users (snowflake),
    language    VARCHAR(2) REFERENCES languages (code),
    proficiency SMALLINT NOT NULL,
    PRIMARY KEY (snowflake, language)
);

CREATE TABLE proficient
(
    snowflake BIGINT REFERENCES users (snowflake),
    language  VARCHAR(5) REFERENCES languages (code),
    PRIMARY KEY (snowflake, language)
);

CREATE TABLE posts
(
    snowflake BIGINT PRIMARY KEY DEFAULT next_snowflake(0, TIMESTAMP '2020-01-01 00:00:00'), -- The ID of the post
    author    BIGINT REFERENCES users (snowflake),                                           -- The author of the post
    content   TEXT NOT NULL                                                                  -- The content of the post
);

CREATE TABLE likes
(
    post  BIGINT REFERENCES posts (snowflake), -- The post liked
    liker BIGINT REFERENCES users (snowflake), -- The user who liked the post
    PRIMARY KEY (post, liker)
);

CREATE TABLE comments
(
    snowflake BIGINT PRIMARY KEY DEFAULT next_snowflake(0, TIMESTAMP '2020-01-01 00:00:00'), -- The comment's ID
    post      BIGINT REFERENCES posts (snowflake),                                           -- The post the comment is on
    commenter BIGINT REFERENCES users (snowflake),                                           -- User the comment is from
    content   TEXT NOT NULL                                                                  -- The content of the comment
);

CREATE TABLE follows
(
    follower BIGINT REFERENCES users (snowflake), -- The person following
    followee BIGINT REFERENCES users (snowflake)  -- The person being followed
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
