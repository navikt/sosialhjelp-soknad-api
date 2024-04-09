CREATE TABLE OPPGAVE (
  id numeric not null,
  behandlingsid varchar(255),
  type varchar(255),
  status varchar(255),
  steg numeric,
  oppgavedata clob,
  oppgaveresultat clob,
  opprettet timestamp,
  sistkjort timestamp,
  nesteforsok timestamp,
  retries numeric,

  CONSTRAINT UNIK_ID UNIQUE (id)
);

CREATE INDEX INDEX_STATUS_FORSOK_ID ON OPPGAVE(status,nesteforsok);

CREATE sequence OPPGAVE_ID_SEQ start WITH 1 increment BY 1;