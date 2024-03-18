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

CREATE TABLE AutomoderateReport
(
    report_id       SERIAL PRIMARY KEY,
    user_id         INT NOT NULL REFERENCES users(user_id),
    passed          BOOLEAN NOT NULL,
    checked_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE topics (
                        topic_id SERIAL PRIMARY KEY,
                        name TEXT NOT NULL
);

CREATE TABLE user_subscriptions (
                                    user_id INT REFERENCES users(user_id),
                                    topic_id INT REFERENCES topics(topic_id),
                                    PRIMARY KEY (user_id, topic_id)
);

ALTER TABLE messages ADD COLUMN topic_id INT REFERENCES topics(topic_id);

CREATE TABLE topic_updates (
                               update_id SERIAL PRIMARY KEY,
                               topic_id INT NOT NULL REFERENCES topics(topic_id),
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE hibernate_sequence;

CREATE TABLE notification (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              message TEXT NOT NULL,
                              FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- CREATE TABLE emails (
--                         id SERIAL PRIMARY KEY,
--                         recipient VARCHAR(255) NOT NULL,
--                         subject VARCHAR(255) NOT NULL,
--                         body TEXT NOT NULL,
--                         sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );