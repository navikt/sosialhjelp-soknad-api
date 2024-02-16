CREATE TABLE soknad
(
    id uuid primary key,
    eier_person_id varchar(50) not null,
    opprettet timestamp not null,
    sist_endret timestamp,
    sendt_inn timestamp,
    hvorfor_soke text,
    hva_sokes_om text,
    utbetalinger_fra_nav bool,
    inntekt_fra_skatt bool,
    stotte_fra_husbanken bool
);

create table nav_enhet
(
    soknad uuid primary key,
    enhetsnavn varchar(255),
    enhetsnummer varchar(30),
    kommunenummer varchar(30),
    orgnummer varchar(30),
    kommunenavn varchar(255),
    constraint fk_navenhet_kontakt
    constraint fk_navenhet_soknad
        foreign key(soknad)
            references soknad(id) on delete cascade
);

CREATE TABLE arbeidsforhold
(
    soknad_key int not null,
    soknad uuid not null,
    arbeidsgivernavn varchar(255) not null,
    orgnummer varchar(50),
    start varchar(50),
    slutt varchar(50),
    fast_stillingsprosent numeric,
    har_fast_stilling boolean,
    constraint pk_arbeidsforhold
        primary key(soknad_key, soknad),
    constraint fk_arbeidsforhold_soknad
        foreign key(soknad)
            references soknad(id) on delete cascade
);