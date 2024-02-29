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

create table nav_enhet
(
    kontakt uuid primary key,
    enhetsnavn varchar(255),
    enhetsnummer varchar(30),
    kommunenummer varchar(30),
    orgnummer varchar(30),
    kommunenavn varchar(255),
    constraint fk_navenhet_soknad
        foreign key(kontakt)
            references kontakt(soknad_id) on delete cascade
);
