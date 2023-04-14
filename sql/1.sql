CREATE SCHEMA habr_abbr_scanner;
SET SEARCH_PATH = 'habr_abbr_scanner';

CREATE TABLE last_scan_post
(
    id int
);
INSERT INTO last_scan_post (id) VALUES (690000);

GRANT USAGE ON SCHEMA habr_abbr_scanner TO habr_abbr_scanner_ro;
GRANT SELECT, UPDATE ON habr_abbr_scanner.last_scan_post TO habr_abbr_scanner_ro;