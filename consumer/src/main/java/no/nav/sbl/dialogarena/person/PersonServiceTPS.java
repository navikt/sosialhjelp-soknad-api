package no.nav.sbl.dialogarena.person;

import javax.inject.Inject;

import no.nav.sbl.dialogarena.person.consumer.transform.PersonTransform;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementer {@link PersonService}. Denne implementasjonen henter data fra TPS
 *
 */
public class PersonServiceTPS implements PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonServiceTPS.class);

    @Inject
    private BrukerprofilPortType brukerProfil;

    @Override
//    @Cacheable(value = PERSON, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#ident)")
    public Person hentPerson(String ident) {
        XMLHentKontaktinformasjonOgPreferanserResponse response = null;
        try {
            response = brukerProfil.hentKontaktinformasjonOgPreferanser(makeXMLRequest(ident));
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS.", e);
           // return Person.ikkeIdentifisert();
            return null;
        } catch (Exception re) {
            logger.error("Kunne ikke hente person med ID {} fra TPS", ident, re);
           // return Person.ikkeIdentifisert();
            return null;
        }
        return new PersonTransform().mapToPerson(response);
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest makeXMLRequest(String ident) {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(ident);
    }
}
