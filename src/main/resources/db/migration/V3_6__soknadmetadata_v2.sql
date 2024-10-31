create table soknad_metadata
(
    soknad_id uuid primary key,
    person_id varchar(30) not null,
    status varchar(50) not null,
    opprettet timestamp not null,
    innsendt timestamp,
    kommunenummer varchar(20),
    bydelsnummer varchar(20),
    digisos_id varchar(50)
)
