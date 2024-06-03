create table dokumentasjon
(
    id uuid primary key,
    soknad_id uuid not null,
    type varchar(50) not null,
    status varchar(50) not null,
    constraint fk_vedlegg_soknad
        foreign key(soknad_id)
            references soknad(id) on delete cascade
);

create table dokument
(
    dokumentasjon uuid not null,
    filnavn varchar(255),
    sha512 varchar(60),
    constraint fk_dokument_dokumentasjon
        foreign key(dokumentasjon)
            references dokumentasjon(id) on delete cascade
);
