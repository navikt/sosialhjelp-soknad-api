ALTER TABLE SoknadBrukerData ADD (foo VARCHAR(1500));
UPDATE SoknadBrukerData SET foo = SUBSTR(value, 1, 1500);
ALTER TABLE SoknadBrukerData drop column value;
ALTER TABLE SoknadBrukerData RENAME column foo to value;