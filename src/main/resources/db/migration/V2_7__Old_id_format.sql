CREATE SEQUENCE id_sequence
    INCREMENT 1
    MINVALUE 4360796
    MAXVALUE 10000000
    START 4360796
    CACHE 100
    NO CYCLE;

ALTER TABLE soknadmetadata
ADD COLUMN id_gammelt_format varchar(50) unique not null;

