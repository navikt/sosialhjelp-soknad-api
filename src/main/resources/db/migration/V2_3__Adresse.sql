create table adresser
(
    soknad_id uuid primary key,
    valgt_adresse varchar(30),
    bruker_adresse text,
    midlertidig_adresse text,
    folkeregistrert_adresse text,
    constraint fk_adresser_soknad
        foreign key(soknad_id)
            references soknad(id) on delete cascade
);
