create table innsendt_soknadmetadata
(
    soknad_id uuid primary key,
    personId varchar(30),
    soknadstype varchar(50),
    sendt_inn_dato timestamp, /* Skal slette alle søknader eldre enn 200 dager */
    opprettet_dato timestamp, /* Skal slette alle søknader eldre enn 200 dager */
        primary key (soknad_id)
)