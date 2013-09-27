package no.nav.sbl.dialogarena.person;

import no.nav.modig.core.exception.SystemException;
import no.nav.sbl.dialogarena.person.consumer.HentBrukerprofilConsumer;
import no.nav.sbl.dialogarena.person.consumer.OppdaterBrukerprofilConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface PersonService {

    Logger LOG = LoggerFactory.getLogger(PersonService.class);

    Person hentPerson(String ident);

    class Default implements PersonService {

        private final HentBrukerprofilConsumer hentConsumer;

        private final OppdaterBrukerprofilConsumer oppdaterConsumer;

        public Default(HentBrukerprofilConsumer hentBrukerprofilConsumer, OppdaterBrukerprofilConsumer oppdaterBrukerprofilConsumer) {
            this.hentConsumer = hentBrukerprofilConsumer;
            this.oppdaterConsumer = oppdaterBrukerprofilConsumer;
        }

        @Override
        public Person hentPerson(final String ident) {
            Person person = hentConsumer.hentPerson(ident);
            if (!person.harIdent(ident)) {
                LOG.error("PersonService.hentPerson: '{}' returnerte bruker med ident: '{}'", ident, person.ident);
                throw new SystemException("Ident returnert fra tjeneste er ikke den som ble forespurt.", null);
            }
            return person;
        }
    }

}
