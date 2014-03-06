create index INDEX_soknad_bbid on soknad(brukerbehandlingid);
create index INDEX_faktum_soknad on soknadbrukerdata(soknad_id);
create index INDEX_faktumegenskap_faktum on faktumegenskap(soknad_id, faktum_id);
create index INDEX_vedlegg_soknad on vedlegg(soknad_id, skjemanummer);
