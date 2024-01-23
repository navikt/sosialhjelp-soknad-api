ALTER TABLE SoknadBrukerData ADD (foo VARCHAR(700));
UPDATE SoknadBrukerData SET foo = SUBSTR(value, 1, 650);
ALTER TABLE SoknadBrukerData drop column value;
ALTER TABLE SoknadBrukerData RENAME column foo to value;