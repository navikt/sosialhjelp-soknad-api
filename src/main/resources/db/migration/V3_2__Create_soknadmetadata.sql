create table soknadmetadata
(
    soknad_id uuid primary key,
    personidentifikator int,
    soknadstype varchar(50),
    sendt_inn_dato timestamp, /* Skal slette alle søknader eldre enn 200 dager */
    opprettet_dato timestamp, /* Skal slette alle søknader eldre enn 200 dager */
        primary key (soknad_id)
)