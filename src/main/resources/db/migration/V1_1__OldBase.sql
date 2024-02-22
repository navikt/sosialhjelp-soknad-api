CREATE TABLE soknad_under_arbeid
(
    soknad_under_arbeid_id bigint primary key generated always as identity,
    versjon numeric(19,0) not null,
    behandlingsid varchar(50) not null,
    tilknyttetbehandlingsid varchar(50),
    eier varchar(30) not null,
    data bytea not null,
    status varchar(30) not null,
    opprettetdato timestamp not null,
    sistendretdato timestamp
);

create table SOKNADMETADATA
(
    id bigint primary key generated always as identity,
    behandlingsId varchar(255) not null,
    tilknyttetBehandlingsId varchar(255),
    skjema varchar(255),
    fnr varchar(255),
    hovedskjema bytea,
    vedlegg bytea,
    orgnr varchar(255),
    navenhet varchar(255),
    fiksforsendelseid varchar(255),
    soknadtype varchar(255),
    innsendingstatus varchar(255),
    opprettetdato timestamp,
    sistendretdato timestamp,
    innsendtdato timestamp,
    batchstatus varchar(255)
)

create table OPPGAVE
(
    id bigint primary key generated always as identity,
    behandlingsid varchar(255),
    type varchar(255),
    status varchar(255),
    steg numeric,
    oppgavedata bytea,
    oppgaveresultat bytea,
    opprettet timestamp,
    sistkjort timestamp,
    nesteforsok timestamp,
    retries numeric
);

CREATE TABLE OPPLASTET_VEDLEGG
(
    UUID VARCHAR(255) NOT NULL,
    EIER VARCHAR(255) NOT NULL,
    TYPE VARCHAR(255) NOT NULL,
    DATA bytea NOT NULL,
    SOKNAD_UNDER_ARBEID_ID bigint NOT NULL,
    FILNAVN VARCHAR(255) NOT NULL,
    SHA512 VARCHAR(255) NOT NULL,
    CONSTRAINT UNIK_OPPLASTET_VEDLEGG_UUID UNIQUE (UUID)
);
