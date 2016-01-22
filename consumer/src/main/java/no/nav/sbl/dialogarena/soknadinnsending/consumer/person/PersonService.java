package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.*;
import no.nav.tjeneste.virksomhet.person.v1.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.*;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.*;

import javax.inject.*;
import javax.xml.ws.*;
import java.util.*;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.FamilierelasjonTransform.mapFamilierelasjon;
import static org.slf4j.LoggerFactory.*;


@Component
public class PersonService {

    private static final Logger logger = getLogger(PersonService.class);

    @Inject
    @Named("personEndpoint")
    private PersonPortType personEndpoint;

    @Inject
    @Named("personSelftestEndpoint")
    private PersonPortType personSelftestEndpoint;

    @Cacheable(value = "barnCache", key = "#fodselsnummer")
    public HentKjerneinformasjonResponse hentKjerneinformasjon(String fodselsnummer) {
        HentKjerneinformasjonRequest request = lagXMLRequestKjerneinformasjon(fodselsnummer);
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

    public List<Barn> hentBarn(String fodselsnummer) {
        try {
            return mapFamilierelasjon(hentKjerneinformasjon(fodselsnummer));
        } catch (IkkeFunnetException e) {
            logger.warn("Ikke funnet person i TPS");
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
        }
        return new ArrayList<>();
    }
    
    public void ping() {
        personSelftestEndpoint.ping();
    }

    private HentKjerneinformasjonRequest lagXMLRequestKjerneinformasjon(String fodselsnummer) {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(fodselsnummer);
        return request;
    }

}
