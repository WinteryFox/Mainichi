CREATE DOMAIN GENDER CHAR(1) CHECK (value IN ('F', 'M', 'U'));

CREATE TABLE users
(
    snowflake BIGINT PRIMARY KEY,                     -- The ID of the user
    username  TEXT   NOT NULL,                        -- The username of the user
    summary   TEXT DEFAULT 'I am a beautiful person', -- The summary/description/self-introduction of the user
    birthday  DATE   NOT NULL,                        -- The user's birthday
    gender    GENDER NOT NULL                         -- The gender of the user (female, male, unknown)
);

CREATE TABLE posts
(
    snowflake BIGINT PRIMARY KEY,                  -- The ID of the post
    author    BIGINT REFERENCES users (snowflake), -- The author of the post
    content   TEXT NOT NULL                        -- The content of the post
);

CREATE TABLE likes
(
    post  BIGINT REFERENCES posts (snowflake), -- The post liked
    liker BIGINT REFERENCES users (snowflake), -- The user who liked the post
    PRIMARY KEY (post, liker)
);

CREATE TABLE comments
(
    snowflake BIGINT PRIMARY KEY,                  -- The comment's ID
    post      BIGINT REFERENCES posts (snowflake), -- The post the comment is on
    commenter BIGINT REFERENCES users (snowflake), -- User the comment is from
    content   TEXT NOT NULL                        -- The content of the comment
);

CREATE TABLE follows
(
    follower BIGINT REFERENCES users (snowflake), -- The person following
    followee BIGINT REFERENCES users (snowflake)  -- The person being followed
);