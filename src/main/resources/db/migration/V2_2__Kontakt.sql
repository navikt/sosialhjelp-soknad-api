create table kontakt
(
    soknad_id uuid primary key,
    telefon_register varchar(50),
    telefon_bruker varchar(50),
    folkeregistrert_adresse text,
    midlertidig_adresse text,
    bruker_adresse text,
    adressevalg varchar(30),
    constraint fk_kontakt_soknad
        foreign key(soknad_id)
            references soknad(id) on delete cascade
);
