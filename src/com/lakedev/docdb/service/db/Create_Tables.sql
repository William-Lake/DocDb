CREATE TABLE doc
(
    id INTEGER NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    data BLOB NOT NULL, 
    add_date TEXT,
    mod_date TEXT
);

CREATE TRIGGER trg_add_date 
AFTER INSERT
ON doc
BEGIN
    UPDATE doc 
    SET add_date = DATETIME('NOW') 
    WHERE id = NEW.id;
END;

CREATE TRIGGER trg_mod_date
AFTER UPDATE
ON doc
BEGIN
    UPDATE doc 
    SET mod_date = DATETIME('NOW')
    WHERE id = NEW.id;
END;