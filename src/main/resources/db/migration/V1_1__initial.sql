CREATE TABLE opplastet_vedlegg (
  uuid varchar(100) primary key,
  eier varchar(255) not null,
  type varchar(255) not null,
  data bytea not null,
  soknad_under_arbeid_id numeric(19,0) not null,
  filnavn varchar(255) not null,
  sha512 varchar(255) not null
);

CREATE TABLE oppgave (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    behandlingsid varchar(30) not null,
    type varchar(30) not null,
    status varchar(30) not null,
    steg int not null,
    oppgavedata text not null,
    oppgaveresultat text not null,
    opprettet timestamp not null ,
    sistkjort timestamp not null ,
    nesteforsok timestamp not null ,
    retries int not null
);

CREATE TABLE soknad_under_arbeid (
    soknad_under_arbeid_id BIGINT GENERATED ALWAYS AS IDENTITY,
    versjon numeric(19,0) not null,
    behandlingsid varchar(50) not null,
    tilknyttetbehandlingsid varchar(50),
    eier varchar(30) not null,
    data bytea not null,
    status varchar(30) not null,
    opprettetdato timestamp not null,
    sistendretdato timestamp
);

CREATE TABLE SOKNADMETADATA (
--     id serial primary key,
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    behandlingsId varchar(255) not null,
    tilknyttetBehandlingsId varchar(255),
    skjema varchar(255),
    fnr varchar(255),
    hovedskjema text,
    vedlegg text,
    orgnr varchar(255),
    navenhet varchar(255),
    fiksforsendelseid varchar(255),
    soknadtype varchar(255),
    innsendingstatus varchar(255),
    opprettetdato timestamp,
    sistendretdato timestamp,
    innsendtdato timestamp,
    batchstatus varchar(255),
    lest_ditt_nav numeric(1,0) default 0 not null
);

-- CREATE sequence METADATA_ID_SEQ start WITH 1 increment BY 1;
-- CREATE sequence SOKNAD_UNDER_ARBEID_ID_SEQ start WITH 1 increment BY 1;

CREATE TABLE soknad (
--   id serial primary key,
--   soknad_id varchar(50) unique,
  id uuid primary key,
  innsendingstidspunkt timestamp,
  hvorfor_soke varchar(255),
  hva_sokes_om varchar(255),
  kommentar_arbeid varchar(255)
);

CREATE TABLE soknad_eier (
    id uuid primary key,
    eier varchar(50) not null
);

CREATE TABLE bosituasjon (
--     id serial primary key,
--     soknad_id varchar(50) unique ,
    soknad_id uuid primary key,
    botype varchar(30),
    antall_personer integer
);

ALTER TABLE bosituasjon
    ADD CONSTRAINT fk_bosituasjon_soknad
        FOREIGN KEY ( soknad_id ) REFERENCES soknad( id )
            ON DELETE CASCADE;


CREATE TABLE vedlegg (
    id                   serial primary key  ,
    soknad_id uuid  NOT NULL,
    vedleggstype varchar(15)    ,
    tilleggsinfo varchar(15)    ,
    status varchar(15)    ,
    hendelse_type varchar(15)            ,
    hendelse_referanse varchar(15)
);

ALTER TABLE vedlegg
    ADD CONSTRAINT fk_vedlegg_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
                ON DELETE CASCADE;

CREATE TABLE fil (
    id serial primary key,
    vedlegg_id numeric not null ,
    filnavn              varchar(50)    ,
    sha512               varchar(50)
);

ALTER TABLE fil
    ADD CONSTRAINT fk_fil_vedlegg
        FOREIGN KEY ( vedlegg_id )
            REFERENCES vedlegg( id )
                ON DELETE CASCADE;
