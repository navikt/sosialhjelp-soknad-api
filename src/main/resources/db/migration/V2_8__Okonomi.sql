create table okonomi
(
    soknad_id uuid primary key,
    constraint fk_okonomi_soknad
        foreign key(soknad_id)
            references soknad(id) on delete cascade
);

create table inntekt
(
    okonomi uuid not null,
    type varchar(50) not null,
    beskrivelse text,
    rader text,
    constraint pk_inntekt
        primary key (okonomi, type),
    constraint fk_inntekt_okonomi
        foreign key(okonomi)
            references okonomi(soknad_id) on delete cascade
);

create table utgift
(
    okonomi uuid not null,
    type varchar(50) not null,
    beskrivelse text,
    rader text,
    constraint pk_utgift
        primary key (okonomi, type),
    constraint fk_utgift_okonomi
        foreign key(okonomi)
            references okonomi(soknad_id) on delete cascade
);

create table formue
(
    okonomi uuid not null,
    type varchar(50) not null,
    beskrivelse text,
    rader text,
    constraint pk_formue
        primary key (okonomi, type),
    constraint fk_formue_okonomi
        foreign key(okonomi)
            references okonomi(soknad_id) on delete cascade
);

create table bekreftelse
(
    okonomi uuid not null,
    type varchar(50) not null,
    verdi boolean not null,
    constraint fk_bekreftelse_okonomi
        foreign key(okonomi)
            references okonomi(soknad_id) on delete cascade
);
