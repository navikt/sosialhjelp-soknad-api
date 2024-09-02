CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

ALTER TABLE dokument ADD COLUMN dokument_id uuid DEFAULT uuid_generate_v4();
UPDATE dokument SET dokument_id = uuid_generate_v4();
ALTER TABLE dokument ADD primary key (dokument_id);
ALTER TABLE dokument
ALTER COLUMN sha512 TYPE varchar(255);