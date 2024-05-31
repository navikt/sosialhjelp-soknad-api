create table okonomi
(
    soknad_id uuid primary key,
    beskrivelse_verdi text,
    beskrivelse_sparing text,
    beskrivelse_utbetaling text,
    beskrivelse_boutgifter text,
    beskrivelse_barneutgifter text,
    constraint fk_okonomi_soknad
        foreign key(soknad_id)
            references soknad(id) on delete cascade
);

create table inntekt
(
    okonomi_key numeric not null,
    okonomi uuid not null,
    type varchar(50) not null,
    tittel varchar(50) not null,
    constraint fk_inntekt_okonomi
        foreign key(okonomi)
            references okonomi(soknad_id) on delete cascade
);

create table utgift
(
    okonomi_key numeric not null,
    okonomi uuid not null,
    type varchar(50) not null,
    tittel varchar(50) not null,
    constraint fk_utgift_okonomi
        foreign key(okonomi)
            references okonomi(soknad_id) on delete cascade
);

create table formue
(
    okonomi_key numeric not null,
    okonomi uuid not null,
    type varchar(50) not null,
    tittel varchar(50) not null,
    constraint fk_formue_okonomi
        foreign key(okonomi)
            references okonomi(soknad_id) on delete cascade
);

create table bekreftelse
(
    okonomi uuid not null,
    type varchar(50) not null,
    tittel varchar(50) not null,
    verdi boolean not null,
    constraint fk_bekreftelse_okonomi
        foreign key(okonomi)
            references okonomi(soknad_id) on delete cascade
);