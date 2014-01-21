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
        personaliaProperties.put("fnr", personalia.getFnr());
        personaliaProperties.put("alder", personalia.getAlder());
        personaliaProperties.put("navn", personalia.getNavn());
        personaliaProperties.put("epost", personalia.getEpost());
        personaliaProperties.put("statsborgerskap", personalia.getStatsborgerskap());
        personaliaProperties.put("kjonn", personalia.getKjonn());
        personaliaProperties.put("gjeldendeAdresse", personalia.getGjeldendeAdresse().getAdresse());
        personaliaProperties.put("gjeldendeAdresseType", personalia.getGjeldendeAdresse().getAdressetype());
        personaliaProperties.put("gjeldendeAdresseGydligFra", personalia.getGjeldendeAdresse().getGyldigFra());
        personaliaProperties.put("gjeldendeAdresseGydligTil", personalia.getGjeldendeAdresse().getGyldigTil());
        personaliaProperties.put("sekundarAdresse", personalia.getSekundarAdresse().getAdresse());
        personaliaProperties.put("sekundarAdresseType", personalia.getSekundarAdresse().getAdressetype());
        personaliaProperties.put("sekundarAdresseGydligFra", personalia.getSekundarAdresse().getGyldigFra());
        personaliaProperties.put("sekundarAdresseGydligTil", personalia.getSekundarAdresse().getGyldigTil());

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
