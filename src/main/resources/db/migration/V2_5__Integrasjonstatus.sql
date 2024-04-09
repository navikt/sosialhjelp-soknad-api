CREATE TABLE integrasjonstatus
(
    soknad_id uuid primary key,
    feil_utbetalinger_nav boolean,
    feil_inntekt_skatteetaten boolean,
    feil_stotte_husbanken boolean,
    constraint fk_integrasjon_soknad
        foreign key (soknad_id) references soknad(id)
            on delete cascade
);
