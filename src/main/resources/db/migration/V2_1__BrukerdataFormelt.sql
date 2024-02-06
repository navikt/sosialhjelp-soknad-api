create table brukerdata_formelt
(
    soknad_id uuid primary key,
    kommentar_arbeidsforhold text,
    er_student bool,
    student_grad varchar(30),
    constraint fk_brukerdata_soknad
        foreign key(soknad_id)
            references soknad(id) on delete cascade
);

-- brukerdata kan ha flere samtykker
create table samtykke
(
    brukerdata_formelt uuid not null,
    type varchar(50) not null,
    verdi bool,
    dato date,
    constraint samtykke_pk primary key(brukerdata_formelt, type),
    constraint fk_samtykke_brukerdata
        foreign key (brukerdata_formelt)
            references brukerdata_formelt(soknad_id) on delete cascade
);

-- brukerdata har 1 beskrivelse av annet
create table beskrivelse_av_annet
(
    brukerdata_formelt uuid primary key,
    barneutgifter text,
    verdier text,
    sparing text,
    utbetalinger text,
    boutgifter text,
    constraint fk_beskrivelser_brukerdata
        foreign key (brukerdata_formelt)
            references brukerdata_formelt(soknad_id) on delete cascade
);
