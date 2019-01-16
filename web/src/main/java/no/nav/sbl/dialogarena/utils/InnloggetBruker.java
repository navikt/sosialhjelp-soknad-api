package no.nav.sbl.dialogarena.utils;

import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.PersonaliaBolk;
import org.slf4j.Logger;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.OidcSubjectHandler.getSubjectHandler;
import static org.slf4j.LoggerFactory.getLogger;

public class InnloggetBruker {

    @Inject
    private PersonaliaBolk personaliaBolk;

    private static final Logger logger = getLogger(InnloggetBruker.class);

    public Personalia hentPersonalia() {
        String fnr = getSubjectHandler().getUserIdFromToken();
        Personalia personalia = null;
        try {
            personalia = personaliaBolk.hentPersonalia(fnr);
        } catch (Exception e) {
            logger.error("Kunne ikke hente personalia");
        }
        return personalia;
    }
}
