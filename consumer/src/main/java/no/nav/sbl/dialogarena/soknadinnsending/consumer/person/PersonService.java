package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.SikkerhetsBegrensningException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v1.HentKjerneinformasjonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v1.PersonPortType;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;

import static org.slf4j.LoggerFactory.getLogger;


@Component
public class PersonService {

    private static final Logger logger = getLogger(PersonService.class);

    @Inject
    @Named("personEndpoint")
    private PersonPortType personEndpoint;

    @Inject
    @Named("personSelftestEndpoint")
    private PersonPortType personSelftestEndpoint;

    @Cacheable(value = "barnCache", key = "#request.ident")
    public HentKjerneinformasjonResponse hentKjerneinformasjon(HentKjerneinformasjonRequest request) {

        try {
            return personEndpoint.hentKjerneinformasjon(request);
        } catch (HentKjerneinformasjonPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS (Person-servicen).", e);
            throw new IkkeFunnetException("fant ikke bruker: " + request.getIdent(), e);
        } catch (HentKjerneinformasjonSikkerhetsbegrensning e) {
            logger.error("Kunne ikke hente bruker fra TPS (Person-servicen).", e);
            throw new SikkerhetsBegrensningException("Kunne ikke hente bruker: " + request.getIdent(), e);
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS (Person-servicen).", e);
            throw new TjenesteUtilgjengeligException("Person", e);
        }
    }
    
    public void ping() {
        personSelftestEndpoint.ping();
    }
}
