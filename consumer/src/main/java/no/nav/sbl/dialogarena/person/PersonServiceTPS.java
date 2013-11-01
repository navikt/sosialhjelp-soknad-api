package no.nav.sbl.dialogarena.person;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implementer {@link PersonService}. Denne implementasjonen henter data fra TPS
 *
 */
public class PersonServiceTPS implements PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonServiceTPS.class);

    @Inject
    @Named("brukerProfilService")
    private BrukerprofilPortType brukerProfil;

	@Inject
	private Kodeverk kodeverk;
    
    @Override
//    @Cacheable(value = PERSON, key = "T(no.nav.sbl.dialogarena.dokumentinnsending.cache.UserKeyGenerator).generate(#ident)")
    public Person hentPerson(Long soknadId, String fodselsnummer) {
        XMLHentKontaktinformasjonOgPreferanserResponse response = null;
        try {
            response = brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequest(fodselsnummer));
            logger.error("Fullstendig XML fra  TPS:" + response);
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS.", e);
            return new Person();
        } catch (Exception re) {
            logger.error("Kunne ikke hente person med ID {} fra TPS", fodselsnummer, re);
            return new Person();
        }
        return new PersonTransform().mapToPerson(soknadId, response, kodeverk);
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest lagXMLRequest(String ident) {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(ident);
    }
}
