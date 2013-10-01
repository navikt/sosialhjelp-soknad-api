package no.nav.sbl.dialogarena.person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PersonService {
    Logger LOG = LoggerFactory.getLogger(PersonService.class);
    Person hentPerson(Long soknadId, String ident);
}
