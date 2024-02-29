create table livssituasjon
(
    soknad_id uuid primary key,
    botype varchar(30),
    antall_husstand numeric,
    er_student boolean,
    studentgrad varchar(30),
    kommentar_arbeidsforhold text,
    constraint fk_livssituasjon_soknad
        foreign key(soknad_id)
            references soknad(id) on delete cascade
);

create table arbeidsforhold
(
    soknad_id uuid not null,
    index numeric not null,
    arbeidsgivernavn varchar(255),
    orgnummer varchar(30),
    start varchar(30),
    slutt varchar(30),
    fast_stillingsprosent numeric,
    har_fast_stilling boolean,
    constraint pk_arbeidsforhold
        primary key (soknad_id, index),
    constraint fk_arbeidsforhold_livssituasjon
        foreign key (soknad_id)
            references livssituasjon(soknad_id) on delete cascade
)