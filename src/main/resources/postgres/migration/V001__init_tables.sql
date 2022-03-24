create table oppgave
(
    id              serial primary key,
    behandlingsid   varchar(255) not null,
    type            varchar(255),
    status          varchar(255),
    steg            integer,
    oppgavedata     varchar(10000),
    oppgaveresultat varchar(10000),
    opprettet       timestamp,
    sistkjort       timestamp,
    nesteforsok     timestamp,
    retries         integer
);

create index index_status_forsok_id on oppgave (status, nesteforsok);
create sequence oppgave_id_seq start with 1 increment by 1;

create table soknad_under_arbeid
(
    soknad_under_arbeid_id  varchar(40)  primary key,
    versjon                 integer      not null default 1,
    behandlingsid           varchar(255) not null unique,
    tilknyttetbehandlingsid varchar(255),
    eier                    varchar(255) not null,
    data                    bytea,
    status                  varchar(255) not null,
    opprettetdato           timestamp    not null,
    sistendretdato          timestamp    not null
);

create index index_soknadua_tilknbehid on soknad_under_arbeid (tilknyttetbehandlingsid);
create index index_soknadua_eier on soknad_under_arbeid (eier);
create index index_soknadua_oppr on soknad_under_arbeid (status, opprettetdato);
create index index_soknadua_endr on soknad_under_arbeid (status, sistendretdato);
create sequence soknad_under_arbeid_id_seq start with 1 increment by 1;

create table opplastet_vedlegg
(
    id                     serial primary key,
    uuid                   varchar(255) not null unique,
    eier                   varchar(255) not null,
    type                   varchar(255) not null,
    data                   bytea        not null,
    soknad_under_arbeid_id varchar(40)  not null,
    filnavn                varchar(255) not null,
    sha512                 varchar(255) not null,
    constraint fk_soknad_under_arbeid foreign key (soknad_under_arbeid_id) references soknad_under_arbeid (soknad_under_arbeid_id)
);

create index index_opplvedl_soknaduaid on opplastet_vedlegg (soknad_under_arbeid_id);

create table sendt_soknad
(
    sendt_soknad_id         serial primary key,
    behandlingsid           varchar(255) not null unique,
    tilknyttetbehandlingsid varchar(255),
    eier                    varchar(255) not null,
    fiksforsendelseid       varchar(255),
    brukeropprettetdato     timestamp    not null,
    brukerferdigdato        timestamp    not null,
    sendtdato               timestamp,
    orgnr                   varchar(255) not null,
    navenhetsnavn           varchar(255) not null
);

create index index_sendtsoknad_tilknbehid on sendt_soknad (tilknyttetbehandlingsid);
create index index_sendtsoknad_eier on sendt_soknad (eier);
create index index_sendtsoknad_oppr on sendt_soknad (brukeropprettetdato);
create index index_sendtsoknad_ferdig on sendt_soknad (brukerferdigdato);
create index index_sendtsoknad_sendt on sendt_soknad (sendtdato);
create sequence sendt_soknad_id_seq start with 1 increment by 1;

create table soknadmetadata
(
    id                      serial primary key,
    behandlingsid           varchar(255) not null,
    tilknyttetbehandlingsid varchar(255),
    skjema                  varchar(255),
    fnr                     varchar(255),
    hovedskjema             varchar(10000),
    vedlegg                 varchar(20000),
    orgnr                   varchar(255),
    navenhet                varchar(255),
    fiksforsendelseid       varchar(255),
    soknadtype              varchar(255),
    innsendingstatus        varchar(255),
    opprettetdato           timestamp,
    sistendretdato          timestamp,
    innsendtdato            timestamp,
    batchstatus             varchar(255) not null default 'LEDIG',
    lest_ditt_nav           boolean      not null default false
);

create index index_metadata_id on soknadmetadata (id);
create index index_metadata_behid on soknadmetadata (behandlingsid);
create index index_tilknyttet_id on soknadmetadata(tilknyttetbehandlingsid);
create sequence metadata_id_seq start with 1 increment by 1;
