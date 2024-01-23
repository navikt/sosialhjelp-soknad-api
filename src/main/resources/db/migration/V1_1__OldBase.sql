CREATE TABLE soknad_under_arbeid
(
    soknad_under_arbeid_id bigint generated always as identity,
    versjon numeric(19,0) not null,
    behandlingsid varchar(50) not null,
    tilknyttetbehandlingsid varchar(50),
    eier varchar(30) not null,
    data bytea not null,
    status varchar(30) not null,
    opprettetdato timestamp not null,
    sistendretdato timestamp
);
