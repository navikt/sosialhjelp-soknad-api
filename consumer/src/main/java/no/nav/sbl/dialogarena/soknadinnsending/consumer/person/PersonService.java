package no.nav.sbl.dialogarena.soknadinnsending.consumer.person;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.StatsborgerskapType;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.*;
import no.nav.tjeneste.virksomhet.person.v1.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Adressetype.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.finnBarnForPerson;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper.mapXmlPersonTilPerson;
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

    public no.nav.sbl.dialogarena.sendsoknad.domain.Person hentPerson(String fodselsnummer) {
        Person person;
        try {
            HentKjerneinformasjonResponse response = hentKjerneinformasjon(fodselsnummer);
            person = response != null ? mapXmlPersonTilPerson(response.getPerson()) : null;
        } catch (IkkeFunnetException e) {
            logger.error("Ikke funnet person i TPS", e);
            throw new ApplicationException("TPS:PersonIkkefunnet", e);
        }
        return person;
    }

    @Cacheable(value = "personCache", key = "#fodselsnummer")
    public HentKjerneinformasjonResponse hentKjerneinformasjon(String fodselsnummer) {
        HentKjerneinformasjonRequest request = lagXMLRequestKjerneinformasjon(fodselsnummer);
        try {
            return personEndpoint.hentKjerneinformasjon(request);
        } catch (HentKjerneinformasjonPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS (Person-servicen).", e);
            throw new IkkeFunnetException("fant ikke bruker: " + request.getIdent(), e);
        } catch (HentKjerneinformasjonSikkerhetsbegrensning e) {
            logger.warn("Kunne ikke hente bruker fra TPS (Person-servicen).", e);
            throw new SikkerhetsBegrensningException("Kunne ikke hente bruker på grunn av manglende tilgang", e);
        } catch (WebServiceException e) {
            logger.warn("Ingen kontakt med TPS (Person-servicen).", e);
            throw new TjenesteUtilgjengeligException("Person", e);
        }
    }

    public List<Barn> hentBarn(String fodselsnummer) {
        try {
            HentKjerneinformasjonResponse response = hentKjerneinformasjon(fodselsnummer);
            if (response != null && response.getPerson() != null) {
                return finnBarnForPerson(response.getPerson());
            }
        } catch (IkkeFunnetException e) {
            logger.warn("Ikke funnet person i TPS");
        } catch (WebServiceException e) {
            logger.warn("Ingen kontakt med TPS.", e);
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
