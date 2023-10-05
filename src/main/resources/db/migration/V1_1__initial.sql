CREATE TABLE opplastet_vedlegg (
  uuid varchar(100) primary key,
  eier varchar(255) not null,
  type varchar(255) not null,
  data blob not null,
  soknad_under_arbeid_id number(19,0) not null,
  filnavn varchar(255) not null,
  sha512 varchar(255) not null
);

CREATE TABLE oppgave (
    id number(19,0) primary key,
    behandlingsid varchar(30) not null,
    type varchar(30) not null,
    status varchar(30) not null,
    steg int not null,
    oppgavedata clob not null,
    oppgaveresultat clob not null,
    opprettet timestamp not null ,
    sistkjort timestamp not null ,
    nesteforsok timestamp not null ,
    retries int not null
);

CREATE TABLE soknad_under_arbeid (
    soknad_under_arbeid_id number(19,0) primary key,
    versjon number(19,0) not null,
    behandlingsid varchar(50) not null,
    tilknyttetbehandlingsid varchar(50),
    eier varchar(30) not null,
    data blob not null,
    status varchar(30) not null,
    opprettetdato timestamp not null,
    sistendretdato timestamp
);

CREATE TABLE SOKNADMETADATA (
    id numeric not null,
    behandlingsId varchar(255) not null,
    tilknyttetBehandlingsId varchar(255),
    skjema varchar(255),
    fnr varchar(255),
    hovedskjema clob,
    vedlegg clob,
    orgnr varchar(255),
    navenhet varchar(255),
    fiksforsendelseid varchar(255),
    soknadtype varchar(255),
    innsendingstatus varchar(255),
    opprettetdato timestamp,
    sistendretdato timestamp,
    innsendtdato timestamp,
    batchstatus varchar(255),
    lest_ditt_nav number(1,0) default 0 not null
);

CREATE sequence METADATA_ID_SEQ start WITH 1 increment BY 1;
CREATE sequence SOKNAD_UNDER_ARBEID_ID_SEQ start WITH 1 increment BY 1;
