package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.PersonaliaBuilder;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AdresseTransform;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.EpostService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.WSKontaktinformasjon;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.WSHentDigitalKontaktinformasjonResponse;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Denne klassen fletter sammen data fra 2 forskjellige endepunkter for å populere
 * domeneobjektet
 */
@Component
public class PersonaliaFletter {

    private static final Logger logger = getLogger(PersonaliaFletter.class);

    @Inject
    private PersonService personService;

    @Inject
    @Named("brukerProfilEndpoint")
    private BrukerprofilPortType brukerProfil;

    @Inject
    private Kodeverk kodeverk;

    @Inject
    private EpostService epostService;

    public Personalia mapTilPersonalia(String fodselsnummer) {
        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse;
        no.nav.sbl.dialogarena.sendsoknad.domain.Person person;
        try {
            preferanserResponse = brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequestPreferanser(fodselsnummer));
            person = personService.hentPerson(fodselsnummer);
        } catch (IkkeFunnetException | HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            logger.error("Ikke funnet person i TPS", e);
            throw new ApplicationException("TPS:PersonIkkefunnet", e);
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            logger.error("Kunne ikke hente bruker fra TPS.", e);
            throw new ApplicationException("TPS:Sikkerhetsbegrensing", e);
        } catch (WebServiceException e) {
            logger.error("Ingen kontakt med TPS.", e);
            throw new ApplicationException("TPS:webserviceException", e);
        }
        if (preferanserResponse == null) {
            return new Personalia();
        }

        XMLBruker xmlBruker = (XMLBruker) preferanserResponse.getPerson();
        WSHentDigitalKontaktinformasjonResponse dkifResponse = epostService.hentInfoFraDKIF(fodselsnummer);

        return PersonaliaBuilder.
                with()
                .fodselsnummer(person.getFnr())
                .fodselsdato(person.getFodselsdato())
                .alder(person.getAlder())
                .diskresjonskode(person.getDiskresjonskode())
                .navn(person.getSammensattNavn())
                .withFornavn(person.getFornavn())
                .withMellomnavn(person.getMellomnavn())
                .withEtternavn(person.getEtternavn())
                .statsborgerskap(person.getStatsborgerskap())
                .kjonn(person.getKjonn())
                .sivilstatus(person.getSivilstatus())
                .ektefelle(personService.hentEktefelle(fodselsnummer))
                .epost(finnEpost(dkifResponse))
                .mobiltelefon(finnMobiltelefonnummer(dkifResponse))
                .gjeldendeAdresse(finnGjeldendeAdresse(xmlBruker, kodeverk))
                .sekundarAdresse(finnSekundarAdresse(xmlBruker, kodeverk))
                .folkeregistrertAdresse(finnFolkeregistrertAdresse(xmlBruker, kodeverk))
                .kontonummer(finnKontonummer(xmlBruker))
                .erUtenlandskBankkonto(erUtenlandskKonto(xmlBruker))
                .utenlandskKontoBanknavn(finnUtenlandsKontoNavn(xmlBruker))
                .utenlandskKontoLand(finnUtenlandskKontoLand(xmlBruker, kodeverk))
                .build();
    }

    private static String finnUtenlandskKontoLand(XMLBruker xmlBruker, Kodeverk kodeverk) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();

        if (bankkonto == null || bankkonto instanceof XMLBankkontoNorge) {
            return "";
        }
        String landkode = ((XMLBankkontoUtland) bankkonto).getBankkontoUtland().getLandkode().getValue();
        return kodeverk.getLand(landkode);
    }

    private static String finnUtenlandsKontoNavn(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();

        if (bankkonto == null || bankkonto instanceof XMLBankkontoNorge) {
            return "";
        }

        return ((XMLBankkontoUtland) bankkonto).getBankkontoUtland().getBanknavn();
    }

    private static Boolean erUtenlandskKonto(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();
        return bankkonto instanceof XMLBankkontoUtland;
    }

    private static String finnKontonummer(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();

        if (bankkonto instanceof XMLBankkontoUtland) {
            return ((XMLBankkontoUtland) bankkonto).getBankkontoUtland().getBankkontonummer();
        } else if (bankkonto instanceof XMLBankkontoNorge) {
            return ((XMLBankkontoNorge) bankkonto).getBankkonto().getBankkontonummer();
        } else {
            return "";
        }
    }

    private static Adresse finnGjeldendeAdresse(XMLBruker xmlBruker, Kodeverk kodeverk) {
        return new AdresseTransform().mapGjeldendeAdresse(xmlBruker, kodeverk);
    }
    
    private static Adresse finnFolkeregistrertAdresse(XMLBruker xmlBruker, Kodeverk kodeverk) {
        return new AdresseTransform().mapFolkeregistrertAdresse(xmlBruker, kodeverk);
    }

    private static Adresse finnSekundarAdresse(XMLBruker xmlBruker, Kodeverk kodeverk) {
        return new AdresseTransform().mapSekundarAdresse(xmlBruker, kodeverk);
    }

    private static String finnMobiltelefonnummer(WSHentDigitalKontaktinformasjonResponse dkifResponse) {
        WSKontaktinformasjon digitalKontaktinformasjon = dkifResponse.getDigitalKontaktinformasjon();
        if (digitalKontaktinformasjon == null || digitalKontaktinformasjon.getMobiltelefonnummer() == null) {
            return "";
        }
        return digitalKontaktinformasjon.getMobiltelefonnummer().getValue();
    }
    
    private static String finnEpost(WSHentDigitalKontaktinformasjonResponse dkifResponse) {
        WSKontaktinformasjon digitalKontaktinformasjon = dkifResponse.getDigitalKontaktinformasjon();
        if (digitalKontaktinformasjon == null || digitalKontaktinformasjon.getEpostadresse() == null) {
            return "";
        }
        return digitalKontaktinformasjon.getEpostadresse().getValue();
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest lagXMLRequestPreferanser(String ident) {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(ident);
    }

}
