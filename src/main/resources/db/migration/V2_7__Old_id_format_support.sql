CREATE SEQUENCE id_sequence
    INCREMENT 1
    MINVALUE 4360796
    MAXVALUE 10000000
    START 4360796
    CACHE 100
    NO CYCLE;

CREATE TABLE id_format_map
(
    soknad_id uuid primary key,
    id_old_format varchar(50) unique
);

