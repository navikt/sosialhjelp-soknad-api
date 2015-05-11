package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.ws.WebServiceException;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class BarnService implements BolkService {


    private static final Logger logger = getLogger(BarnService.class);
    private static final String BOLKNAVN = "Barn";

    @Inject
    private PersonService personService;
    @Inject
    private FaktaService faktaService;

    @Override
    public String tilbyrBolk() {
        return BOLKNAVN;
    }

    @Override
    @Cacheable("barnCache")
    public void lagreBolk(String fodselsnummer, Long soknadId) {
        HentKjerneinformasjonResponse kjerneinformasjonResponse;

        try {
            kjerneinformasjonResponse = personService.hentKjerneinformasjon(lagXMLRequestKjerneinformasjon(fodselsnummer));
            List<Barn> barn = FamilierelasjonTransform.mapFamilierelasjon(kjerneinformasjonResponse);
            lagreBarn(soknadId, barn);

        } catch (IkkeFunnetException e) {
            logger.warn("Ikke funnet person i TPS");
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
        }
    }


    private void lagreBarn(Long soknadId, List<Barn> barneliste) {

        for (Barn barn : barneliste) {
            Faktum barneFaktum = new Faktum().medSoknadId(soknadId).medKey("barn").medType(SYSTEMREGISTRERT)
                    .medSystemProperty("fornavn", barn.getFornavn())
                    .medSystemProperty("mellomnavn", barn.getMellomnavn())
                    .medSystemProperty("etternavn", barn.getEtternavn())
                    .medSystemProperty("sammensattnavn", barn.getSammensattnavn())
                    .medSystemProperty("fnr", barn.getFnr())
                    .medSystemProperty("kjonn", barn.getKjonn())
                    .medSystemProperty("alder", barn.getAlder().toString())
                    .medSystemProperty("land", barn.getLand());
            faktaService.lagreSystemFaktum(soknadId, barneFaktum, "fnr");
        }
    }

    private HentKjerneinformasjonRequest lagXMLRequestKjerneinformasjon(String ident) {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(ident);
        return request;
    }
}
