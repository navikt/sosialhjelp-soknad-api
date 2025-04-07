ALTER TABLE soknad
ADD CONSTRAINT fk_soknad
FOREIGN KEY (id) REFERENCES soknad_metadata(soknad_id)
ON DELETE CASCADE;