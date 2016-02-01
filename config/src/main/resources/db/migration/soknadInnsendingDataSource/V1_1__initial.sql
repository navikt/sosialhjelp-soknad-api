CREATE TABLE soknad (
  soknad_id     NUMBER(19, 0)             NOT NULL,
  brukerbehandlingId VARCHAR(255 CHAR)    NOT NULL,
  navSoknadId   VARCHAR(255 CHAR)         NOT NULL,
  aktorId		VARCHAR(255 CHAR)		  NOT NULL,		
  opprettetDato TIMESTAMP DEFAULT sysdate NOT NULL,
  status		VARCHAR2(255)			  NOT NULL,
  CONSTRAINT soknad_pk PRIMARY KEY (soknad_id)
)
PARTITION BY RANGE (opprettetDato)
INTERVAL (NUMTOYMINTERVAL(3, 'month'))
(
  PARTITION Q1_2013
    VALUES LESS THAN (TO_DATE('1-4-2013', 'DD-MM-YYYY'))
);

CREATE TABLE SoknadBrukerData (
  soknadBrukerData_id NUMBER(19,0) NOT NULL,
  soknad_id           NUMBER(19,0) NOT NULL,
  KEY varchar(255 CHAR) NOT NULL,
  VALUE varchar(255 CHAR) NULL,
  sistEndret timestamp NOT NULL,
  type varchar(255),
  constraint SoknadBrukerData_PK primary key(soknadBrukerData_id),
  constraint SoknadBrukerDataSoknad_FK foreign key(SOKNAD_ID) references SOKNAD(soknad_id)
)
PARTITION BY RANGE (sistEndret)
INTERVAL (NUMTOYMINTERVAL(3, 'month'))
(
  PARTITION Q1_2013
    VALUES LESS THAN (TO_DATE('1-4-2013', 'DD-MM-YYYY'))
);

create sequence SOKNAD_ID_SEQ start with 1 increment by 1;
create sequence SOKNAD_BRUKER_DATA_ID_SEQ start with 1 increment by 1;
