create table innsendt_soknadmetadata
(
    soknad_id uuid primary key,
    person_id varchar(30),
    soknadstype varchar(50),
    sendt_inn_dato timestamp, /* Skal slette alle søknader eldre enn 200 dager */
    opprettet_dato timestamp /* Skal slette alle søknader eldre enn 200 dager */
)