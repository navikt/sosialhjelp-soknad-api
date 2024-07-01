create table situasjonsendring
(
    soknad_id uuid primary key,
    hva_er_endret text,
    endring bool,
    CONSTRAINT fk_situasjonsendring_soknad
        FOREIGN KEY (soknad_id)
            REFERENCES soknad (id)
            ON DELETE CASCADE
)
