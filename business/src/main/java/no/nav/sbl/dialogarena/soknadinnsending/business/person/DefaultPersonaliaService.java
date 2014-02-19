package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.EosBorgerService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
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
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.ALDER_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.EPOST_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.FNR_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_GYLDIGFRA_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_GYLDIGTIL_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.GJELDENDEADRESSE_TYPE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.KJONN_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.NAVN_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.SEKUNDARADRESSE_GYLDIGFRA_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.SEKUNDARADRESSE_GYLDIGTIL_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.SEKUNDARADRESSE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.SEKUNDARADRESSE_TYPE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.STATSBORGERSKAPTYPE_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia.STATSBORGERSKAP_KEY;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class DefaultPersonaliaService implements PersonaliaService {

    private static final Logger logger = getLogger(DefaultPersonaliaService.class);
    @Inject
    @Named("brukerProfilService")
    private BrukerprofilPortType brukerProfil;
    @Inject
    private PersonConnector personConnector;
    @Inject
    private Kodeverk kodeverk;
    @Inject
    private SendSoknadService soknadService;
    @Inject
    private EosBorgerService eosBorgerService;

    //TODO: Må fikses, ikke returnere tom personalia når manglende svar fra TPS.
    @Override
    public Personalia hentPersonalia(String fodselsnummer) {
//    public Personalia hentPersonalia(String fodselsnummer) throws IkkeFunnetException, HentKontaktinformasjonOgPreferanserPersonIkkeFunnet, HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning, WebServiceException {
        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse;
        HentKjerneinformasjonResponse kjerneinformasjonResponse;

        try {
            kjerneinformasjonResponse = personConnector.hentKjerneinformasjon(lagXMLRequestKjerneinformasjon(fodselsnummer));
            logger.warn("Respons fra TPS" + kjerneinformasjonResponse.toString());
            preferanserResponse = brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequestPreferanser(fodselsnummer));
        } catch (IkkeFunnetException | HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            logger.error("Ikke funnet person i TPS", e);
            //throw new ApplicationException("TPS:PersonIkkefunnet",e);
            return new Personalia();
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            logger.error("Kunne ikke hente bruker fra TPS.", e);
            //throw new ApplicationException("TPS:Sikkerhetsbegrensing",e);
            return new Personalia();
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
            // throw new ApplicationException("TPS:webserviceException",e);
            return new Personalia();
        }
        return PersonaliaTransform.mapTilPersonalia(preferanserResponse, kjerneinformasjonResponse, kodeverk);
    }

    @Override
    public Personalia lagrePersonaliaOgBarn(String fodselsnummer, Long soknadId) {
        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse;
        HentKjerneinformasjonResponse kjerneinformasjonResponse;

        try {
            logger.warn("Kaller kjerneinformasjon " + fodselsnummer);
            kjerneinformasjonResponse = personConnector.hentKjerneinformasjon(lagXMLRequestKjerneinformasjon(fodselsnummer));
            logger.warn("Kaller preferanser " + fodselsnummer);
            preferanserResponse = brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequestPreferanser(fodselsnummer));
            logger.warn("Kalt TPS-tjenestene");
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

        logger.warn("TPS-tjenester kalt");
        Personalia personalia = PersonaliaTransform.mapTilPersonalia(preferanserResponse, kjerneinformasjonResponse, kodeverk);
        List<Barn> barn = FamilierelasjonTransform.mapFamilierelasjon(kjerneinformasjonResponse);

        lagrePersonalia(soknadId, personalia);
        lagreBarn(soknadId, barn);

        return personalia;
    }

    private void lagrePersonalia(Long soknadId, Personalia personalia) {
        String statsborgerskap = personalia.getStatsborgerskap();

        Faktum personaliaFaktum = new Faktum().medSoknadId(soknadId).medKey("personalia")
                .medSystemProperty(FNR_KEY, personalia.getFnr())
                .medSystemProperty(ALDER_KEY, personalia.getAlder())
                .medSystemProperty(NAVN_KEY, personalia.getNavn())
                .medSystemProperty(EPOST_KEY, personalia.getEpost())
                .medSystemProperty(STATSBORGERSKAP_KEY, statsborgerskap)
                .medSystemProperty(STATSBORGERSKAPTYPE_KEY, eosBorgerService.getStatsborgeskapType(statsborgerskap))
                .medSystemProperty(KJONN_KEY, personalia.getKjonn())
                .medSystemProperty(GJELDENDEADRESSE_KEY, personalia.getGjeldendeAdresse().getAdresse())
                .medSystemProperty(GJELDENDEADRESSE_TYPE_KEY, personalia.getGjeldendeAdresse().getAdressetype())
                .medSystemProperty(GJELDENDEADRESSE_GYLDIGFRA_KEY, personalia.getGjeldendeAdresse().getGyldigFra())
                .medSystemProperty(GJELDENDEADRESSE_GYLDIGTIL_KEY, personalia.getGjeldendeAdresse().getGyldigTil())
                .medSystemProperty(SEKUNDARADRESSE_KEY, personalia.getSekundarAdresse().getAdresse())
                .medSystemProperty(SEKUNDARADRESSE_TYPE_KEY, personalia.getSekundarAdresse().getAdressetype())
                .medSystemProperty(SEKUNDARADRESSE_GYLDIGFRA_KEY, personalia.getSekundarAdresse().getGyldigFra())
                .medSystemProperty(SEKUNDARADRESSE_GYLDIGTIL_KEY, personalia.getSekundarAdresse().getGyldigTil());

        soknadService.lagreSystemFaktum(soknadId, personaliaFaktum, "fnr");
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
            soknadService.lagreSystemFaktum(soknadId, barneFaktum, "fnr");
        }
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
