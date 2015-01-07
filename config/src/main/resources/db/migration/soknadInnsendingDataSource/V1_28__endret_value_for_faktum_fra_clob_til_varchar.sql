ALTER TABLE SoknadBrukerData ADD (foo VARCHAR(500));
UPDATE SoknadBrukerData SET foo = value;
ALTER TABLE SoknadBrukerData drop column value;
ALTER TABLE SoknadBrukerData RENAME column foo to value;