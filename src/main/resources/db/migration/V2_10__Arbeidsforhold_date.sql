ALTER TABLE arbeidsforhold DROP COLUMN start;
ALTER TABLE arbeidsforhold DROP COLUMN slutt;

ALTER TABLE arbeidsforhold
    ADD COLUMN start date,
    ADD COLUMN slutt date;
