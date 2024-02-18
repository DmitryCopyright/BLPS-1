CREATE TABLE users
(
    user_id     SERIAL PRIMARY KEY,
    email       TEXT NOT NULL,
    password    TEXT NOT NULL,
    name        TEXT NOT NULL
);

CREATE TABLE messages
(
    message_id           SERIAL PRIMARY KEY,
    user_id             INT REFERENCES users ON DELETE CASCADE,
    text_message         TEXT
);