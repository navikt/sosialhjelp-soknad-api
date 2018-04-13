ALTER TABLE SoknadBrukerData ADD (foo CLOB);
UPDATE SoknadBrukerData SET foo = value;
ALTER TABLE SoknadBrukerData drop column value;
ALTER TABLE SoknadBrukerData RENAME column foo to value;