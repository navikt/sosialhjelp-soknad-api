CREATE TABLE familie
(
    soknad_id          uuid primary key,
    har_forsorgerplikt boolean,
    barnebidrag        varchar(30),
    sivilstatus        varchar(30),
    CONSTRAINT fk_familie_soknad
        FOREIGN KEY (soknad_id)
            REFERENCES soknad (id)
            ON DELETE CASCADE
);

CREATE TABLE barn
(
    familie_key            uuid primary key,
    familie                uuid         not null,
    person_id              varchar(30)  not null,
    fornavn                varchar(255) not null,
    mellomnavn             varchar(255),
    etternavn              varchar(255) not null,
    fodselsdato            varchar(20),
    bor_sammen             boolean,
    folkeregistrert_sammen boolean,
    delt_bosted            boolean,
    samvarsgrad            numeric,
    CONSTRAINT fk_barn_familie
        FOREIGN KEY (familie)
            REFERENCES familie (soknad_id)
            ON DELETE CASCADE
);

CREATE TABLE ektefelle
(
    familie                       uuid primary key,
    person_id                     varchar(30)  not null,
    fornavn                       varchar(255) not null,
    mellomnavn                    varchar(255),
    etternavn                     varchar(255) not null,
    fodselsdato                   varchar(20),
    har_diskresjonskode           boolean,
    folkeregistrert_med_ektefelle boolean,
    bor_sammen                    boolean,
    kilde_er_system               boolean not null,
    CONSTRAINT fk_ektefelle_familie
        FOREIGN KEY (familie)
            REFERENCES familie (soknad_id)
            ON DELETE CASCADE
);
