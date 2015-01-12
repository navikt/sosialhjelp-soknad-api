ALTER TABLE SoknadBrukerData ADD (foo VARCHAR(600));
UPDATE SoknadBrukerData SET foo = SUBSTR(value, 1, 600);
ALTER TABLE SoknadBrukerData drop column value;
ALTER TABLE SoknadBrukerData RENAME column foo to value;