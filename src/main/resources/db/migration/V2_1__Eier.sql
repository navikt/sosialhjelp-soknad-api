CREATE TABLE eier
(
    soknad_id uuid primary key,
    fornavn varchar(255) not null,
    mellomnavn varchar(255),
    etternavn varchar(255) not null,
    statsborgerskap varchar(50),
    nordisk_borger bool,
    konto_bruker varchar(50),
    konto_register varchar(50),
    har_ikke_konto bool,
    constraint fk_eier_soknad
        foreign key (soknad_id)
            references soknad(id) on delete cascade
);
