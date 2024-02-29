CREATE TABLE soknad
(
    id uuid primary key,
    eier_person_id varchar(50) not null,
    opprettet timestamp not null,
    sist_endret timestamp,
    sendt_inn timestamp,
    hvorfor_soke text,
    hva_sokes_om text,
    utbetalinger_fra_nav boolean,
    inntekt_fra_skatt boolean,
    stotte_fra_husbanken boolean
);
