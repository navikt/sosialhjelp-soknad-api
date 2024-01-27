create table brukerdata
(
    soknad_id uuid primary key,
    telefonnummer varchar(50),
    kommentar_arbeidsforhold text,
    kontonummer varchar(50),
    har_ikke_konto bool,
    hvorfor_soke text,
    hva_sokes_om text,
    constraint fk_brukerdata_soknad
        foreign key(soknad_id)
            references soknad(id) on delete cascade
);

create table samtykke
(
    brukerdata uuid not null,
    type varchar(50) not null,
    verdi bool,
    dato date,
    constraint samtykke_pk primary key(brukerdata, type),
    constraint fk_samtykke_brukerdata
        foreign key (brukerdata)
            references brukerdata(soknad_id) on delete cascade
);

create table beskrivelse_av_annet
(
    brukerdata uuid primary key,
    barneutgifter text,
    verdier text,
    sparing text,
    utbetalinger text,
    boutgifter text,
    constraint fk_beskrivelser_brukerdata
        foreign key (brukerdata)
            references brukerdata(soknad_id) on delete cascade
);
