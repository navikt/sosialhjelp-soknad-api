package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Barn;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Personalia lagrePersonaliaOgBarn(String fodselsnummer, Long soknadId) {
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

        Personalia personalia = PersonaliaTransform.mapTilPersonalia(preferanserResponse, kjerneinformasjonResponse, kodeverk);
        List<Barn> barn = NewFamilierelasjonTransform.mapFamilierelasjon(kjerneinformasjonResponse);

        lagrePersonalia(soknadId, personalia);
        lagreBarn(soknadId, barn);

        return personalia;
    }

    private void lagrePersonalia(Long soknadId, Personalia personalia) {
        Map<String, String> personaliaProperties = new HashMap<>();
        personaliaProperties.put(FNR_KEY, personalia.getFnr());
        personaliaProperties.put(ALDER_KEY, personalia.getAlder());
        personaliaProperties.put(NAVN_KEY, personalia.getNavn());
        personaliaProperties.put(EPOST_KEY, personalia.getEpost());
        personaliaProperties.put(STATSBORGERSKAP_KEY, personalia.getStatsborgerskap());
        personaliaProperties.put(KJONN_KEY, personalia.getKjonn());
        personaliaProperties.put(GJELDENDEADRESSE_KEY, personalia.getGjeldendeAdresse().getAdresse());
        personaliaProperties.put(GJELDENDEADRESSE_TYPE_KEY, personalia.getGjeldendeAdresse().getAdressetype());
        personaliaProperties.put(GJELDENDEADRESSE_GYLDIGFRA_KEY, personalia.getGjeldendeAdresse().getGyldigFra());
        personaliaProperties.put(GJELDENDEADRESSE_GYLDIGTIL_KEY, personalia.getGjeldendeAdresse().getGyldigTil());
        personaliaProperties.put(SEKUNDARADRESSE_KEY, personalia.getSekundarAdresse().getAdresse());
        personaliaProperties.put(SEKUNDARADRESSE_TYPE_KEY, personalia.getSekundarAdresse().getAdressetype());
        personaliaProperties.put(SEKUNDARADRESSE_GYLDIGFRA_KEY, personalia.getSekundarAdresse().getGyldigFra());
        personaliaProperties.put(SEKUNDARADRESSE_GYLDIGTIL_KEY, personalia.getSekundarAdresse().getGyldigTil());

        Faktum personaliaFaktum = new Faktum(soknadId, null, "personalia", "");
        personaliaFaktum.setProperties(personaliaProperties);
        soknadService.lagreSystemFaktum(soknadId, personaliaFaktum, "");
    }

    @SuppressWarnings("unchecked")
    private void lagreBarn(Long soknadId, List<Barn> barneliste) {

        for (Barn barn : barneliste) {
            Faktum barneFaktum = new Faktum(soknadId, null, "barn", null, SYSTEMREGISTRERT.name());
            Map<String, String> properties = new HashMap<>();
            properties.put("fornavn", barn.getFornavn());
            properties.put("mellomnavn", barn.getMellomnavn());
            properties.put("etternavn", barn.getEtternavn());
            properties.put("sammensattnavn", barn.getSammensattnavn());
            properties.put("fnr", barn.getFnr());
            properties.put("kjonn", barn.getKjonn());
            properties.put("alder", barn.getAlder().toString());
            barneFaktum.setProperties(properties);
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
