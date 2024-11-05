create table soknad_metadata
(
    soknad_id uuid primary key,
    person_id varchar(30) not null,
    status varchar(50) not null,
    opprettet timestamp not null,
    sist_endret timestamp not null,
    sendt_inn timestamp,
    mottaker_kommunenummer varchar(20),
    digisos_id varchar(50),
    soknad_type varchar(50) not null
)
