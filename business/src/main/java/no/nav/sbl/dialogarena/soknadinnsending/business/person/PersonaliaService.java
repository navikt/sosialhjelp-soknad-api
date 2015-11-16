package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.StatsborgerskapType;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.DigitalKontaktinformasjonV1;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.HentDigitalKontaktinformasjonSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonRequest;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonRequest;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.*;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class PersonaliaService implements BolkService {

    private static final Logger logger = getLogger(PersonaliaService.class);
    private static final String BOLKNAVN = "Personalia";

    @Inject
    @Named("brukerProfilEndpoint")
    private BrukerprofilPortType brukerProfil;
    @Inject
    @Named("dkifService")
    private DigitalKontaktinformasjonV1 dkif;
    @Inject
    private PersonService personService;
    @Inject
    private Kodeverk kodeverk;


    public Personalia hentPersonalia(String fodselsnummer) {
        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse = null;
        HentKjerneinformasjonResponse kjerneinformasjonResponse = null;
        WSHentDigitalKontaktinformasjonResponse dkifResponse = new WSHentDigitalKontaktinformasjonResponse();

        try {
            kjerneinformasjonResponse = personService.hentKjerneinformasjon(lagXMLRequestKjerneinformasjon(fodselsnummer));
            preferanserResponse = brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequestPreferanser(fodselsnummer));
            dkifResponse = hentInfoFraDKIF(fodselsnummer);

        } catch (IkkeFunnetException | HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            logger.error("Ikke funnet person i TPS", e);
            throw new ApplicationException("TPS:PersonIkkefunnet", e);
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            logger.error("Kunne ikke hente bruker fra TPS.", e);
            throw new ApplicationException("TPS:Sikkerhetsbegrensing", e);
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
            throw new ApplicationException("TPS:webserviceException", e);
        } catch (HentDigitalKontaktinformasjonSikkerhetsbegrensing | HentDigitalKontaktinformasjonPersonIkkeFunnet e) {
            logger.error("Person ikke tilgjengelig i dkif", e);
            throw new ApplicationException("Dkif:webserviceException", e);
        } catch (HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet e) {
           logger.info("Kunne ikke hente kontaktinformasjon fra dkif");
        }

        return PersonaliaTransform.mapTilPersonalia(preferanserResponse, kjerneinformasjonResponse, kodeverk, dkifResponse);
    }

    @Override
    public String tilbyrBolk() {
        return BOLKNAVN;
    }



    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        try {
            Personalia personalia = hentPersonalia(fodselsnummer);
            return genererPersonaliaFaktum(soknadId, personalia);
        } catch (ApplicationException e) {
            return new ArrayList<>();
        }
    }

    private List<Faktum> genererPersonaliaFaktum(Long soknadId, Personalia personalia) {
        String statsborgerskap = personalia.getStatsborgerskap();
        return Arrays.asList(new Faktum().medSoknadId(soknadId).medKey("personalia")
                .medSystemProperty(FNR_KEY, personalia.getFnr())
                .medSystemProperty(KONTONUMMER_KEY, personalia.getKontonummer())
                .medSystemProperty(ER_UTENLANDSK_BANKKONTO, personalia.getErUtenlandskBankkonto().toString())
                .medSystemProperty(UTENLANDSK_KONTO_BANKNAVN, personalia.getUtenlandskKontoBanknavn())
                .medSystemProperty(UTENLANDSK_KONTO_LAND, personalia.getUtenlandskKontoLand())
                .medSystemProperty(ALDER_KEY, personalia.getAlder())
                .medSystemProperty(NAVN_KEY, personalia.getNavn())
                .medSystemProperty(EPOST_KEY, personalia.getEpost())
                .medSystemProperty(STATSBORGERSKAP_KEY, statsborgerskap)
                .medSystemProperty(STATSBORGERSKAPTYPE_KEY, StatsborgerskapType.get(statsborgerskap))
                .medSystemProperty(KJONN_KEY, personalia.getKjonn())
                .medSystemProperty(GJELDENDEADRESSE_KEY, personalia.getGjeldendeAdresse().getAdresse())
                .medSystemProperty(DISKRESJONSKODE, personalia.getDiskresjonskode())
                .medSystemProperty(GJELDENDEADRESSE_TYPE_KEY, personalia.getGjeldendeAdresse().getAdressetype())
                .medSystemProperty(GJELDENDEADRESSE_GYLDIGFRA_KEY, personalia.getGjeldendeAdresse().getGyldigFra())
                .medSystemProperty(GJELDENDEADRESSE_GYLDIGTIL_KEY, personalia.getGjeldendeAdresse().getGyldigTil())
                .medSystemProperty(GJELDENDEADRESSE_LANDKODE, personalia.getGjeldendeAdresse().getLandkode())
                .medSystemProperty(SEKUNDARADRESSE_KEY, personalia.getSekundarAdresse().getAdresse())
                .medSystemProperty(SEKUNDARADRESSE_TYPE_KEY, personalia.getSekundarAdresse().getAdressetype())
                .medSystemProperty(SEKUNDARADRESSE_GYLDIGFRA_KEY, personalia.getSekundarAdresse().getGyldigFra())
                .medSystemProperty(SEKUNDARADRESSE_GYLDIGTIL_KEY, personalia.getSekundarAdresse().getGyldigTil()));
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest lagXMLRequestPreferanser(String ident) {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(ident);
    }

    private HentKjerneinformasjonRequest lagXMLRequestKjerneinformasjon(String ident) {
        HentKjerneinformasjonRequest request = new HentKjerneinformasjonRequest();
        request.setIdent(ident);
        return request;
    }

    protected WSHentDigitalKontaktinformasjonResponse hentInfoFraDKIF(String ident) throws HentDigitalKontaktinformasjonKontaktinformasjonIkkeFunnet, HentDigitalKontaktinformasjonSikkerhetsbegrensing, HentDigitalKontaktinformasjonPersonIkkeFunnet {
        return dkif.hentDigitalKontaktinformasjon(makeDKIFRequest(ident));
    }

    private WSHentDigitalKontaktinformasjonRequest makeDKIFRequest(String ident) {
        return new WSHentDigitalKontaktinformasjonRequest().withPersonident(ident);
    }

}
