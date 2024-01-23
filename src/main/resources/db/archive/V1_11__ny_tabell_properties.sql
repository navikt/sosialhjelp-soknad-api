CREATE TABLE faktumegenskap (
  faktumegenskap_id NUMBER(19, 0)     NOT NULL,
  soknad_id         NUMBER(19, 0)     NOT NULL,
  faktum_id         NUMBER(19, 0)     NOT NULL,
  key               VARCHAR(255 CHAR) NOT NULL,
  value             VARCHAR(255 CHAR),
  CONSTRAINT faktumegenskap_pk PRIMARY KEY (soknad_id, faktum_id, key)
);
CREATE SEQUENCE FAKTUMEGENSKAP_ID_SEQ START WITH 1 INCREMENT BY 1;
