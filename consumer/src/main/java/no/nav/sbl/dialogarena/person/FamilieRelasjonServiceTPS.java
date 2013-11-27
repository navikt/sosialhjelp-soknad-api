package no.nav.sbl.dialogarena.person;

import static org.slf4j.LoggerFactory.getLogger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;

import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;

import org.slf4j.Logger;

/**
 * Implementer {@link PersonService}. Denne implementasjonen henter data fra TPS
 *
 */
public class FamilieRelasjonServiceTPS implements FamilieRelasjonService {

    private static final Logger logger = getLogger(FamilieRelasjonServiceTPS.class);

    @Inject
    @Named("personService")
    private PersonPortType person;

	/**
	 * Forsøker å hente person fra TPS og transformere denne til vår Personmodell.
	 * Dersom det feiler, logges feilen og det returneres et tomt Person objekt videre 
	 * 
	 */
    @Override
    public Person hentPerson(Long soknadId, String fodselsnummer) {
    	HentKjerneinformasjonResponse response = null;
        try {
            response = person.hentKjerneinformasjon(lagXMLRequest(fodselsnummer));
            logger.warn("Fullstendig XML fra Person-servicen:" + response);
        } catch (HentKjerneinformasjonPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS.", e);
            return new Person();
        } catch (HentKjerneinformasjonSikkerhetsbegrensning e) {
        	logger.error("Kunne ikke hente bruker fra TPS.", e);
            return new Person();
		} catch(WebServiceException e) {
			logger.error("Ingen kontakt med TPS.", e);
            return new Person();
		}
        
        return new Person();
        //return new PersonTransform().mapToPerson(soknadId, response);
    }

    private HentKjerneinformasjonRequest lagXMLRequest(String ident) {
    	HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
    	request.setIdent(ident);
        return request;
    }
}
