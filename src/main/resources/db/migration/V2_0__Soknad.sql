CREATE TABLE soknad
(
    id uuid primary key default gen_random_uuid(),
    innsendingstidspunkt timestamp,
    person_id varchar(30) not null,
    fornavn varchar(255) not null,
    mellomnavn varchar(255),
    etternavn varchar(255) not null,
    statsborgerskap varchar(50),
    nordisk_borger bool,
    kontonummer varchar(30),
    telefonnummer varchar(30)
);

CREATE TABLE nav_enhet
(
    soknad uuid primary key,
    kommunenummer varchar(10) not null,
    orgnummer varchar(30) not null,
    enhetsnummer varchar(30) not null,
    enhetsnavn text not null,
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
    har_fast_stilling bool,
    constraint pk_arbeidsforhold
        primary key(soknad_key, soknad),
    constraint fk_arbeidsforhold_soknad
        foreign key(soknad)
            references soknad(id) on delete cascade
);
