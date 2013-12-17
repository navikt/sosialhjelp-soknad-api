package no.nav.sbl.dialogarena.person;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;

import com.google.gson.Gson;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementer {@link PersonService}. Denne implementasjonen henter data fra TPS, og lagrer som systemfaktum i databasen
 *
 */
public class FamilieRelasjonServiceTPS implements FamilieRelasjonService {

    private static final Logger logger = getLogger(FamilieRelasjonServiceTPS.class);

    @Inject
    @Named("personService")
    private PersonPortType person;

    @Inject
    private SendSoknadService soknadService;
    
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
            if (response != null)
            {
            logger.warn("Fullstendig XML fra Person-servicen:" + response.getPerson());
            }
        } catch (HentKjerneinformasjonPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS (Person-servicen).", e);
            return new Person();
        } catch (HentKjerneinformasjonSikkerhetsbegrensning e) {
        	logger.error("Kunne ikke hente bruker fra TPS (Person-servicen).", e);
            return new Person();
		} catch(WebServiceException e) {
			logger.error("Ingen kontakt med TPS (Person-servicen).", e);
            return new Person();
		}
    
       Person person = new FamilieRelasjonTransform().mapFamilierelasjonTilPerson(soknadId, response);
       
       lagreBarn(soknadId, person);
       
       return person;
    }

    @SuppressWarnings("unchecked")
	private void lagreBarn(Long soknadId, Person person) {
    	List<Barn> barneliste = (List<Barn>) person.getFakta().get("barn");
    	if(barneliste != null) {
	    	for (Barn barn : barneliste) {
				soknadService.lagreSystemSoknadsFelt(soknadId, "barn", new Gson().toJson(barn));
			}
    	}
	}

	private HentKjerneinformasjonRequest lagXMLRequest(String ident) {
    	HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
    	request.setIdent(ident);
        return request;
    }
}
