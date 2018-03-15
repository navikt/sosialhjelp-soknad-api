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
    innsendtdato timestamp
);

CREATE INDEX INDEX_SOKNADMETADATA_ID on SOKNADMETADATA(id);
CREATE INDEX INDEX_SOKNADMETADATA_BEHANDLINGSID on SOKNADMETADATA(behandlingsId);