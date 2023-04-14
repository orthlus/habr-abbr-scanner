CREATE SCHEMA habr_abbr_scanner;
SET SEARCH_PATH = 'habr_abbr_scanner';
CREATE TABLE posts
(
    id int PRIMARY KEY,
    has_abbr boolean NOT NULL,
    timestamp timestamp DEFAULT timezone('Europe/Moscow'::text, CURRENT_TIMESTAMP)
);
-- INSERT INTO posts (id, has_abbr) VALUES (690002,false);
CREATE TABLE telegram_messages
(
    post_id int PRIMARY KEY,
    timestamp timestamp DEFAULT timezone('Europe/Moscow'::text, CURRENT_TIMESTAMP)
);
CREATE TABLE not_found_posts
(
    id int PRIMARY KEY,
    timestamp timestamp DEFAULT timezone('Europe/Moscow'::text, CURRENT_TIMESTAMP)
);
CREATE TABLE access_denied_posts
(
    id int PRIMARY KEY,
    timestamp timestamp DEFAULT timezone('Europe/Moscow'::text, CURRENT_TIMESTAMP)
);

GRANT USAGE ON SCHEMA habr_abbr_scanner TO habr_abbr_scanner_ro;
GRANT SELECT, INSERT ON ALL TABLES IN SCHEMA habr_abbr_scanner TO habr_abbr_scanner_ro;