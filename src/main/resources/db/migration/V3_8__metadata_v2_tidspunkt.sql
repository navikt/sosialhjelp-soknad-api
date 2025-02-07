ALTER TABLE soknad_metadata
    RENAME COLUMN innsendt TO sendt_inn;
ALTER TABLE soknad_metadata
    ADD COLUMN sist_endret timestamp not null DEFAULT current_timestamp,
    ADD COLUMN soknad_type varchar(50) not null DEFAULT 'STANDARD';


