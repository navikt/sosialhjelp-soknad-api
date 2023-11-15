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
    innsendingstidspunkt timestamp
);

CREATE TABLE eier
(
    soknad uuid primary key,
    person_id varchar(30) not null,
    fornavn varchar(255),
    mellomnavn varchar(255),
    etternavn varchar(255),
    statsborgerskap varchar(50),
    nordisk_borger bool,
    kontonummer varchar(30),
    telefonnummer varchar(30),
    folkeregistrert_adresse varchar(255),
    midlertidig_adresse varchar(255)
);

ALTER TABLE eier
    ADD CONSTRAINT fk_eier_soknad
        FOREIGN KEY ( soknad ) REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE brukerdata
(
    soknad_id uuid primary key,
    valgt_adresse varchar(30),
    oppholdsadresse varchar(255)
);

ALTER TABLE brukerdata
    ADD CONSTRAINT fk_brukerdata_soknad
        FOREIGN KEY ( soknad_id ) REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE samtykke
(
    brukerdata uuid not null,
    brukerdata_key varchar(50) not null,
    verdi bool not null,
    bekreftelsesdato date not null
);

ALTER TABLE samtykke
    ADD CONSTRAINT pk_samtykke_brukerdata
        PRIMARY KEY (brukerdata, brukerdata_key);

ALTER TABLE samtykke
    ADD CONSTRAINT fk_samtykke_soknad
        FOREIGN KEY ( brukerdata ) REFERENCES brukerdata( soknad_id )
            ON DELETE CASCADE;

CREATE TABLE brukerdata_key_value
(
    brukerdata uuid not null,
    key varchar(50) not null,
    value varchar(255)
);

ALTER TABLE brukerdata_key_value
    ADD CONSTRAINT pk_brukerdata_value
        PRIMARY KEY (brukerdata, key);

ALTER TABLE brukerdata_key_value
    ADD CONSTRAINT fk_brukerdata_value_brukerdata
        FOREIGN KEY ( brukerdata ) REFERENCES brukerdata( soknad_id )
            ON DELETE CASCADE;

CREATE TABLE arbeidsforhold
(
    id uuid primary key,
    soknad_id uuid not null,
    orgnummer varchar(50),
    arbeidsgivernavn varchar(255),
    fra_og_med varchar(50),
    til_og_med varchar(50),
    stillingsprosent numeric,
    stillingstype varchar(50)
);

ALTER TABLE arbeidsforhold
    ADD CONSTRAINT fk_arbeidsforhold_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
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
    vedlegg_type varchar(30)    ,
    status varchar(15)    ,
    hendelse_type varchar(30)            ,
    hendelse_referanse varchar(50)
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

CREATE TABLE sivilstand
(
    soknad_id uuid primary key,
    kilde varchar(30),
    sivilstatus varchar(30)
);

ALTER TABLE sivilstand
    ADD CONSTRAINT fk_sivilstand_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE ektefelle
(
    sivilstand uuid primary key,
    person_id varchar(30) not null,
    fornavn varchar(255),
    mellomnavn varchar(255),
    etternavn varchar(255),
    fodselsdato varchar(30),
    har_diskresjonskode bool,
    bor_sammen bool,
    folkeregistrert_med bool
);

ALTER TABLE ektefelle
    ADD CONSTRAINT fk_ektefelle_sivilstand
        FOREIGN KEY ( sivilstand )
            REFERENCES sivilstand( soknad_id )
            ON DELETE CASCADE;

CREATE TABLE forsorger
(
    soknad_id uuid primary key,
    har_forsorgerplikt bool,
    barnebidrag varchar(30)
);

ALTER TABLE forsorger
    ADD CONSTRAINT fk_forsorger_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE barn
(
    forsorger uuid not null,
    person_id varchar(30) not null,
    fornavn varchar(255),
    mellomnavn varchar(255),
    etternavn varchar(255),
    fodselsdato date,
    bor_sammen bool,
    folkeregistrert_med bool,
    delt_bosted bool,
    samvarsgrad numeric
);

ALTER TABLE barn
    ADD CONSTRAINT pk_barn
        PRIMARY KEY (forsorger, person_id);

ALTER TABLE barn
    ADD CONSTRAINT fk_barn_forsorgeransvar
        FOREIGN KEY ( forsorger )
            REFERENCES forsorger( soknad_id )
            ON DELETE CASCADE;

CREATE TABLE utgift
(
    id uuid primary key,
    soknad_id uuid not null,
    type varchar(50),
    tittel varchar(50),
    belop numeric
);

ALTER TABLE utgift
    ADD CONSTRAINT fk_utgift_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE formue
(
    id uuid primary key,
    soknad_id uuid not null,
    type varchar(50),
    tittel varchar(50),
    belop numeric
);

ALTER TABLE formue
    ADD CONSTRAINT fk_formue_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE bekreftelse
(
    id uuid primary key,
    soknad_id uuid not null,
    type varchar(50),
    tittel varchar(50),
    bekreftet bool,
    dato timestamp
);

ALTER TABLE bekreftelse
    ADD CONSTRAINT fk_bekreftelse_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE inntekt
(
    id uuid primary key,
    soknad_id uuid not null,
    type varchar(50),
    tittel varchar(50),
    brutto numeric,
    netto numeric
);

ALTER TABLE inntekt
    ADD CONSTRAINT fk_inntekt_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE utbetaling
(
    inntekt uuid primary key,
    kilde varchar(15) not null,
    orgnummer varchar(30),
    belop numeric,
    skattetrekk numeric,
    andre_trekk numeric,
    dato timestamp,
    periode_start timestamp,
    periode_slutt timestamp
);

ALTER TABLE utbetaling
    ADD CONSTRAINT fk_utbetaling_inntekt
        FOREIGN KEY ( inntekt )
            REFERENCES inntekt( id )
            ON DELETE CASCADE;

CREATE TABLE komponent
(
    inntekt uuid,
    type varchar(30),
    belop numeric,
    sats_type varchar(30),
    sats_antall numeric,
    sats_belop numeric
);

ALTER TABLE komponent
    ADD CONSTRAINT fk_komponent_utbetaling
        FOREIGN KEY ( inntekt )
            REFERENCES utbetaling( inntekt )
            ON DELETE CASCADE;

CREATE TABLE utdanning
(
    soknad_id uuid primary key,
    er_student boolean not null,
    student_grad varchar(50)
);

ALTER TABLE utdanning
    ADD CONSTRAINT fk_utdanning_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;

CREATE TABLE bostotte
(
    id uuid primary key,
    soknad_id uuid not null,
    type varchar(50),
    dato date,
    status varchar(50),
    beskrivelse varchar(255),
    vedtaksstatus varchar(50)
);

ALTER TABLE bostotte
    ADD CONSTRAINT fk_bostotte_soknad
        FOREIGN KEY ( soknad_id )
            REFERENCES soknad( id )
            ON DELETE CASCADE;
