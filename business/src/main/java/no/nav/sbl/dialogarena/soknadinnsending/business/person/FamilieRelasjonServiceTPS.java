package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonConnector;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementer {@link PersonService}. Denne implementasjonen henter data fra TPS, og lagrer som systemfaktum i databasen
 */
@Service
public class FamilieRelasjonServiceTPS implements FamilieRelasjonService {

    private static final Logger logger = getLogger(FamilieRelasjonServiceTPS.class);
    @Inject
    private SendSoknadService soknadService;
    @Inject
    private PersonConnector personConnector;

    /**
     * Forsøker å hente person fra TPS og transformere denne til vår Personmodell.
     * Dersom det feiler, logges feilen og det returneres et tomt Person objekt videre
     */
    @Override
    public Person hentPerson(Long soknadId, String fodselsnummer) {
        HentKjerneinformasjonResponse response;
        try {
            response = personConnector.hentKjerneinformasjon(lagXMLRequest(fodselsnummer));
        } catch (IkkeFunnetException e) {
            logger.warn("Ikke funnet person i TPS");
            return new Person();
        }
        if (response != null)
        {
            logger.warn("Fullstendig respons fra Person-servicen:" + response.getPerson());
        } else
        {
            logger.warn("Respons fra Person-servicen er null");
        }
        //if (response != null && logger.isDebugEnabled()) {
        //    logger.debug("Fullstendig XML fra Person-servicen:" + response.getPerson());
       // }
        Person person = new FamilieRelasjonTransform().mapFamilierelasjonTilPerson(soknadId, response);

        lagreBarn(soknadId, person);
        lagreStatsborgerskap(soknadId, person);
        return person;
    }

    private void lagreStatsborgerskap(Long soknadId, Person person) {
        String statsborgerskap = (String)person.getFakta().get("statsborgerskap");
        
        if((statsborgerskap != null) && (!statsborgerskap.isEmpty())) {
            Faktum statsborgerskapFaktum = new Faktum(soknadId, null, "statsborgerskap", statsborgerskap, FaktumType.SYSTEMREGISTRERT.toString());
            Map<String, String> properties = new HashMap<>();
            statsborgerskapFaktum.setProperties(properties);
            soknadService.lagreSystemFaktum(soknadId, statsborgerskapFaktum, "fnr");
        }
    }

    @SuppressWarnings("unchecked")
    private void lagreBarn(Long soknadId, Person person) {
        List<Barn> barneliste = (List<Barn>) person.getFakta().get("barn");

        if (barneliste != null) {
            for (Barn barn : barneliste) {
                Faktum barneFaktum = new Faktum(soknadId, null, "barn", null, FaktumType.SYSTEMREGISTRERT.toString());
                Map<String, String> properties = new HashMap<>();
                properties.put("fornavn", barn.getFornavn());
                properties.put("mellomnavn", barn.getMellomnavn());
                properties.put("etternavn", barn.getEtternavn());
                properties.put("sammensattnavn", barn.getSammensattnavn());
                properties.put("fnr", barn.getFnr());
                properties.put("kjonn", barn.getKjonn());
                properties.put("alder", barn.getAlder().toString());
                properties.put("land", barn.getLand());
                barneFaktum.setProperties(properties);

                soknadService.lagreSystemFaktum(soknadId, barneFaktum, "fnr");
            }
        }
    }

    private HentKjerneinformasjonRequest lagXMLRequest(String ident) {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(ident);
        return request;
    }

    @Override
    public boolean ping() {
        try {
            personConnector.ping();
            return true;
        } catch (RuntimeException e) {
            return false;
        }

    }
}
