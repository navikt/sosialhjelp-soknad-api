package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonConnector;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class DefaultPersonaliaService implements PersonaliaService {

    private static final Logger logger = getLogger(PersonServiceTPS.class);

    @Inject
    @Named("brukerProfilService")
    private BrukerprofilPortType brukerProfil;

    @Inject
    private PersonConnector personConnector;

    @Inject
    private Kodeverk kodeverk;

    @Override
    public Personalia hentPersonalia(String fodselsnummer) {
        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse;
        HentKjerneinformasjonResponse kjerneinformasjonResponse;

        try {
            kjerneinformasjonResponse = personConnector.hentKjerneinformasjon(lagXMLRequestKjerneinformasjon(fodselsnummer));
            preferanserResponse = brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequestPreferanser(fodselsnummer));
        } catch (IkkeFunnetException e) {
            logger.warn("Ikke funnet person i TPS");
            return new Personalia();
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            logger.error("Fant ikke bruker i TPS.", e);
            return new Personalia();
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            logger.error("Kunne ikke hente bruker fra TPS.", e);
            return new Personalia();
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
            return new Personalia();
        }
        return PersonaliaTransform.mapTilPersonalia(preferanserResponse, kjerneinformasjonResponse, kodeverk);
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest lagXMLRequestPreferanser(String ident) {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(ident);
    }

    private HentKjerneinformasjonRequest lagXMLRequestKjerneinformasjon(String ident) {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(ident);
        return request;
    }
}
