create table oppgave
(
    id serial primary key,
    behandlingsid varchar(255) not null,
    type varchar(255),
    status varchar(255),
    steg integer,
    oppgavedata varchar(10000),
    oppgaveresultat varchar(10000),
    opprettet timestamp,
    sistkjort timestamp,
    nesteforsok timestamp,
    retries integer
);

create table soknad_under_arbeid
(
    id serial primary key,
    soknad_under_arbeid_id varchar(40) not null unique,
    versjon integer not null default 1,
    behandlingsid varchar(255) not null,
    tilknyttetbehandlingsid varchar(255),
    eier varchar(255) not null,
    data bytea,
    status varchar(255) not null,
    opprettetdato timestamp not null,
    sistendretdato timestamp not null
);

create table opplastet_vedlegg
(
    id serial primary key,
    uuid varchar(255) not null unique,
    eier varchar(255) not null,
    type varchar(255) not null,
    data bytea not null,
    soknad_under_arbeid_id varchar(40) not null,
    filnavn varchar(255) not null,
    sha512 varchar(255) not null,
    constraint fk_soknad_under_arbeid
        foreign key(soknad_under_arbeid_id)
            references soknad_under_arbeid(soknad_under_arbeid_id)
);

create table sendtsoknad
(
    sendt_soknad_id serial primary key,
    behandlingsid varchar(255) not null,
    tilknyttetbehandlingsid varchar(255),
    eier varchar(255) not null,
    fiksforsendelseid varchar(255),
    brukeropprettetdato timestamp not null,
    brukerferdigdato timestamp not null,
    sendtdato timestamp,
    orgnr varchar(255) not null,
    navenhetsnavn varchar(255) not null,
);

create table soknadmetadata
(
    id serial primary key,
    behandlingsid varchar(255) not null,
    tilknyttetbehandlingsid varchar(255),
    skjema varchar(255),
    fnr varchar(255),
    hovedskjema varchar(10000),
    vedlegg varchar(20000),
    orgnr varchar(255),
    navenhet varchar(255),
    fiksforsendelseid varchar(255),
    soknadtype varchar(255),
    innsendingstatus varchar(255),
    opprettetdato timestamp,
    sistendretdato timestamp,
    innsendtdato timestamp,
    batchstatus varchar(255),
    lest_ditt_nav boolean not null
);
