package no.nav.sbl.dialogarena.person;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementer {@link PersonService}. Denne implementasjonen henter data fra TPS
 *
 */
public class PersonServiceTPS implements PersonService {

    private static final Logger logger = getLogger(PersonServiceTPS.class);

    @Inject
    @Named("brukerProfilService")
    private BrukerprofilPortType brukerProfil;

	@Inject
	private Kodeverk kodeverk;
    
	/**
	 * Forsøker å hente person fra TPS og transformere denne til vår Personmodell.
	 * Dersom det feiler, logges feilen og det returneres et tomt Person objekt videre 
	 * 
	 */
    @Override
    public Person hentPerson(Long soknadId, String fodselsnummer) {
        XMLHentKontaktinformasjonOgPreferanserResponse response = null;
        try {
            response = brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequest(fodselsnummer));
            logger.warn("Fullstendig XML fra  TPS:" + response);
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS.", e);
            return new Person();
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
        	logger.error("Kunne ikke hente bruker fra TPS.", e);
            return new Person();
		} 
        return new PersonTransform().mapToPerson(soknadId, response, kodeverk);
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest lagXMLRequest(String ident) {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(ident);
    }
}
