create table soknad_metadata
(
    soknad_id uuid primary key,
    person_id varchar(30),
    status varchar(50),
    opprettet timestamp, /* Skal slette alle søknader eldre enn 200 dager */
    innsendt timestamp, /* Skal slette alle søknader eldre enn 200 dager */
    kommunenummer varchar(20),
    bydelsnummer varchar(20)
)
