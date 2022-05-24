CREATE DATABASE telegram;
CREATE USER telegram_user WITH PASSWORD 'telegram_user_password';
GRANT USAGE ON SCHEMA public to telegram_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO telegram_user;

CREATE TABLE notes
(
    user_id   bigint,
    note      text,
    timestamp timestamp DEFAULT timezone('Europe/Moscow', CURRENT_TIMESTAMP)
);
CREATE TABLE users
(
    user_id bigint PRIMARY KEY,
    notes_start timestamp,
    notes_end timestamp
);
CREATE OR REPLACE FUNCTION add_note() RETURNS trigger
AS
$$
BEGIN
    IF EXISTS(SELECT FROM users WHERE user_id = NEW.user_id)
    THEN
        UPDATE users
        SET notes_end = NEW.timestamp
        WHERE user_id = NEW.user_id;
    ELSE
        INSERT INTO users (user_id, notes_start, notes_end)
        VALUES (NEW.user_id, NEW.timestamp, NEW.timestamp);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS new_note_trigger on notes;
CREATE TRIGGER new_note_trigger AFTER INSERT on notes
    FOR EACH ROW EXECUTE FUNCTION add_note();