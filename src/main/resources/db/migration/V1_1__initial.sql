CREATE TABLE opplastet_vedlegg
(
    uuid varchar(100) primary key,
    eier varchar(255) not null,
    type varchar(255) not null,
    data bytea not null,
    soknad_under_arbeid_id numeric(19,0) not null,
    filnavn varchar(255) not null,
    sha512 varchar(255) not null
);

CREATE TABLE oppgave
(
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

CREATE TABLE soknad_under_arbeid
(
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

CREATE TABLE SOKNADMETADATA
(
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

-- TODO se kommentar under
-- utoincrement is not postgresql. You want a integer primary key generated always as identity
-- (or serial if you use PG 9 or lower. serial was soft-deprecated in PG 10).

CREATE TABLE soknad
(
    id uuid primary key,
    eier varchar(255) not null,
    innsendingstidspunkt timestamp,
    hvorfor_soke varchar(255),
    hva_sokes_om varchar(255),
    adresse_valg varchar(255)
);

CREATE TABLE arbeid (
                        soknad_id uuid primary key,
                        kommentar_arbeid varchar(255)
);

ALTER TABLE arbeid
    ADD CONSTRAINT fk_arbeid_soknad
        FOREIGN KEY ( soknad_id ) REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE arbeidsforhold
(
    orgnummer varchar(50),
    arbeidsgivernavn varchar(255) not null,
    fra_og_med varchar(50),
    til_og_med varchar(50),
    stillingsprosent numeric,
    stillingstype varchar(50) not null,
    soknad_id uuid not null
);

ALTER TABLE arbeidsforhold
    ADD CONSTRAINT fk_arbeidsforhold_arbeid
        FOREIGN KEY ( soknad_id ) REFERENCES arbeid( soknad_id )
            ON DELETE CASCADE;

CREATE TABLE bosituasjon
(
    soknad_id uuid primary key,
    botype varchar(30),
    antall_personer integer
);

ALTER TABLE bosituasjon
    ADD CONSTRAINT fk_bosituasjon_soknad
        FOREIGN KEY ( soknad_id ) REFERENCES soknad( id )
            ON DELETE CASCADE;


CREATE TABLE vedlegg
(
    id serial primary key  ,
    soknad_id uuid  NOT NULL,
    type varchar(30)    ,
    status varchar(15)    ,
    hendelse_type varchar(15)            ,
    hendelse_referanse varchar(15)
);

ALTER TABLE vedlegg
    ADD CONSTRAINT fk_vedlegg_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE fil
(
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

create table adresse_for_soknad
(
    soknad_id uuid not null,
    type_adressevalg varchar(30) not null,
    adresse_type varchar(30) not null,
    adresse_json text not null
);

ALTER TABLE adresse_for_soknad
    ADD CONSTRAINT pk_adresse_for_soknad
        PRIMARY KEY (soknad_id, type_adressevalg);


ALTER TABLE adresse_for_soknad
    ADD CONSTRAINT fk_afs_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

-- Knytter person til soknad slik at databasen kan fjerne personinnslag når soknad slettes
-- Dette kan føre til at det forekommer duplikate personer i listen - men siden informasjonen uansett ligger i
-- en begrenset periode, antas overhead å være minimal
CREATE TABLE person_for_soknad
(
    person_id varchar(30) not null,
    soknad_id uuid not null,
    fornavn varchar(255),
    mellomnavn varchar(255),
    etternavn varchar(255),
    statsborgerskap varchar(50),
    nordisk_borger bool,
    fodselsdato varchar(30)
);

ALTER TABLE person_for_soknad
    ADD CONSTRAINT pk_person_for_soknad
        PRIMARY KEY (person_id, soknad_id);

ALTER TABLE person_for_soknad
    ADD CONSTRAINT fk_pfs_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE familie (
    soknad_id uuid primary key,
    har_forsorgerplikt bool,
    barnebidrag varchar(30),
    sivilstatus varchar(30)
);

ALTER TABLE familie
    ADD CONSTRAINT fk_familie_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE barn (
    soknad_id uuid not null,
    person_id varchar(30) not null,
    bor_sammen bool,
    folkeregistrert_sammen bool,
    delt_bosted bool,
    samvarsgrad numeric
);

ALTER TABLE barn
    ADD CONSTRAINT pk_barn
        PRIMARY KEY (soknad_id, person_id);

ALTER TABLE barn
    ADD CONSTRAINT fk_barn_familie
        FOREIGN KEY ( soknad_id )
            REFERENCES familie( soknad_id )
            ON DELETE CASCADE;

CREATE TABLE ektefelle (
    soknad_id uuid primary key,
    person_id varchar(30) not null ,
    har_diskresjonskode bool,
    folkeregistrert_med_ektefelle bool,
    bor_sammen bool
);

ALTER TABLE ektefelle
    ADD CONSTRAINT fk_ektefelle_familie
        FOREIGN KEY ( soknad_id )
            REFERENCES familie( soknad_id )
            ON DELETE CASCADE;
