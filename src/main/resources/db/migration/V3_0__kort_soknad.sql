alter table SOKNADMETADATA
    add column is_kort_soknad boolean default false not null;

alter table soknad
    add column is_kort_soknad boolean default false not null;
