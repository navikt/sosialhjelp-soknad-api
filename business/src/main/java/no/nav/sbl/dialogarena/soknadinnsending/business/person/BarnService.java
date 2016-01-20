package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.FamilierelasjonTransform;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.modig.lang.collections.IterUtils.on;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class BarnService implements BolkService {

    private static final Logger logger = getLogger(BarnService.class);
    private static final String BOLKNAVN = "Barn";

    @Inject
    private PersonService personService;

    @Override
    public String tilbyrBolk() {
        return BOLKNAVN;
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        HentKjerneinformasjonResponse kjerneinformasjonResponse;

        try {
            kjerneinformasjonResponse = personService.hentKjerneinformasjon(lagXMLRequestKjerneinformasjon(fodselsnummer));
            List<Barn> barn = FamilierelasjonTransform.mapFamilierelasjon(kjerneinformasjonResponse);
            return genererBarnFakta(soknadId, barn);

        } catch (IkkeFunnetException e) {
            logger.warn("Ikke funnet person i TPS");
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
        }
        return new ArrayList<>();
    }

    private List<Faktum> genererBarnFakta(final Long soknadId, List<Barn> barneliste) {
        return on(barneliste).map(new Transformer<Barn, Faktum>() {
            @Override
            public Faktum transform(Barn barn) {
                return new Faktum().medSoknadId(soknadId).medKey("barn").medType(Faktum.FaktumType.SYSTEMREGISTRERT)
                        .medSystemProperty("fornavn", barn.getFornavn())
                        .medSystemProperty("mellomnavn", barn.getMellomnavn())
                        .medSystemProperty("etternavn", barn.getEtternavn())
                        .medSystemProperty("sammensattnavn", barn.getSammensattnavn())
                        .medSystemProperty("fnr", barn.getFnr())
                        .medSystemProperty("kjonn", barn.getKjonn())
                        .medSystemProperty("alder", barn.getAlder().toString())
                        .medSystemProperty("land", barn.getLand())
                        .medUnikProperty("fnr");
            }
        }).collect();
    }

    private HentKjerneinformasjonRequest lagXMLRequestKjerneinformasjon(String ident) {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(ident);
        return request;
    }
}
