CREATE TABLE Vedlegg (
  vedlegg_id     NUMBER(19, 0)            NOT NULL,
  soknad_id     NUMBER(19, 0)             NOT NULL,
  faktum  number(10, 0)                   NOT NULL,
  navn VARCHAR(255 CHAR)                  NOT NULL,
  storrelse  NUMBER(19, 0)                NOT NULL,
  data  blob                              NOT NULL,
  opprettetDato TIMESTAMP DEFAULT sysdate NOT NULL,

  CONSTRAINT vedlegg_pk PRIMARY KEY (vedlegg_id)
);

create sequence VEDLEGG_ID_SEQ start with 1 increment by 1;
