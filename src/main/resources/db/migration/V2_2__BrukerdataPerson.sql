create table brukerdata_person
(
    soknad_id uuid primary key,
    telefonnummer varchar(50),
    kontonummer varchar(50),
    har_ikke_konto bool,
    hvorfor_soke text,
    hva_sokes_om text,
    botype varchar(30),
    antall_husstand numeric,
    constraint fk_brukerdata_soknad
        foreign key(soknad_id)
            references soknad(id) on delete cascade
);
