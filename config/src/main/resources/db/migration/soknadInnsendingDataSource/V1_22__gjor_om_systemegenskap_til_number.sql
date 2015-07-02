alter table faktumegenskap drop column systemegenskap;
alter table faktumegenskap add systemegenskap number(1) default 0 check (systemegenskap in (0,1));