CREATE TABLE soknad
(
    id uuid primary key default gen_random_uuid(),
    innsendingstidspunkt timestamp
);

CREATE TABLE eier
(
    soknad uuid primary key,
    person_id varchar(30) not null,
    fornavn varchar(255) not null,
    mellomnavn varchar(255),
    etternavn varchar(255) not null,
    statsborgerskap varchar(50),
    nordisk_borger bool,
    kontonummer varchar(30),
    telefonnummer varchar(30),
    constraint fk_eier_soknad
        foreign key (soknad)
            references soknad(id) on delete cascade
);
