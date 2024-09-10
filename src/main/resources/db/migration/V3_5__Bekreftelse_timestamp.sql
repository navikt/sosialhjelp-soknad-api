ALTER TABLE bekreftelse DROP COLUMN dato;

ALTER TABLE bekreftelse
    ADD COLUMN tidspunkt timestamp DEFAULT NOW()
