CREATE TABLE opplysning_expectation
(
    soknad_id       UUID NOT NULL,
    opplysning_type TEXT NOT NULL,
    CONSTRAINT fk_opplysning_soknad
        FOREIGN KEY (soknad_id)
            REFERENCES soknad (id)
            ON DELETE CASCADE,
    CONSTRAINT pk_opplysning_expectation
        PRIMARY KEY (soknad_id, opplysning_type)
)