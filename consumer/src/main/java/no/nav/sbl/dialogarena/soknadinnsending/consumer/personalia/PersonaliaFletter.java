package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import no.nav.modig.core.exception.*;
import no.nav.sbl.dialogarena.kodeverk.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.digitalkontaktinformasjon.v1.meldinger.*;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.*;
import org.joda.time.*;
import org.slf4j.*;
import org.springframework.stereotype.*;

import javax.inject.*;
import javax.xml.ws.*;

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

    private static final String KJONN_MANN = "m";
    private static final String KJONN_KVINNE = "k";

    public Personalia mapTilPersonalia(String fodselsnummer) {
        XMLHentKontaktinformasjonOgPreferanserResponse preferanserResponse;
        HentKjerneinformasjonResponse kjerneinformasjonResponse;
        try {
            preferanserResponse = brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequestPreferanser(fodselsnummer));
            kjerneinformasjonResponse = personService.hentKjerneinformasjon(fodselsnummer);
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
        Person xmlPerson = kjerneinformasjonResponse.getPerson();
        Diskresjonskoder diskresjonskode = kjerneinformasjonResponse.getPerson().getDiskresjonskode();
        String diskresjonskodeString = diskresjonskode == null ? null : diskresjonskode.getValue();

        WSHentDigitalKontaktinformasjonResponse dkifResponse = epostService.hentInfoFraDKIF(fodselsnummer);

        return PersonaliaBuilder.
                with()
                .fodselsnummer(finnFnr(xmlBruker))
                .fodselsdato(finnFodselsdato(xmlPerson))
                .alder(finnAlder(finnFnr(xmlBruker)))
                .diskresjonskode(diskresjonskodeString)
                .navn(finnSammensattNavn(xmlBruker))
                .epost(finnEpost(dkifResponse))
                .statsborgerskap(finnStatsborgerskap(xmlPerson))
                .kjonn(finnKjonn(xmlBruker))
                .gjeldendeAdresse(finnGjeldendeAdresse(xmlBruker, kodeverk))
                .sekundarAdresse(finnSekundarAdresse(xmlBruker, kodeverk))
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

    private static Adresse finnSekundarAdresse(XMLBruker xmlBruker, Kodeverk kodeverk) {
        return new AdresseTransform().mapSekundarAdresse(xmlBruker, kodeverk);
    }

    private static String finnStatsborgerskap(Person xmlPerson) {
        if (xmlPerson.getStatsborgerskap() != null) {
            Statsborgerskap statsborgerskap = xmlPerson.getStatsborgerskap();
            return statsborgerskap.getLand().getValue();
        } else {
            return "NOR";
        }
    }

    private static LocalDate finnFodselsdato(Person person) {
        return new LocalDate(person.getFoedselsdato().getFoedselsdato().toGregorianCalendar());
    }

    private static String finnEpost(WSHentDigitalKontaktinformasjonResponse dkifResponse) {
        WSKontaktinformasjon digitalKontaktinformasjon = dkifResponse.getDigitalKontaktinformasjon();
        if (digitalKontaktinformasjon == null || digitalKontaktinformasjon.getEpostadresse() == null) {
            return "";
        }
        return digitalKontaktinformasjon.getEpostadresse().getValue();    }

    private static String finnFnr(XMLBruker xmlBruker) {
        return xmlBruker.getIdent().getIdent();
    }

    private static String finnAlder(String fnr) {
        return String.valueOf(new PersonAlder(fnr).getAlder());
    }

    private static String finnKjonn(XMLBruker xmlBruker) {
        return Character.getNumericValue(finnFnr(xmlBruker).charAt(8)) % 2 == 0 ? KJONN_KVINNE : KJONN_MANN;
    }

    private static String finnSammensattNavn(XMLBruker xmlBruker) {
        if (fornavnExists(xmlBruker)) {
            return finnFornavn(xmlBruker) + finnMellomNavn(xmlBruker) + finnEtterNavn(xmlBruker);
        } else {
            return finnEtterNavn(xmlBruker);
        }
    }

    private static String finnFornavn(XMLBruker xmlBruker) {
        return fornavnExists(xmlBruker) ? xmlBruker.getPersonnavn().getFornavn() + " " : "";
    }

    private static boolean fornavnExists(XMLBruker xmlBruker) {
        return xmlBruker.getPersonnavn() != null && xmlBruker.getPersonnavn().getFornavn() != null;
    }

    private static String finnMellomNavn(XMLBruker xmlBruker) {
        return mellomnavnExists(xmlBruker) ? xmlBruker.getPersonnavn().getMellomnavn() + " " : "";
    }

    private static boolean mellomnavnExists(XMLBruker xmlBruker) {
        return xmlBruker.getPersonnavn() != null && xmlBruker.getPersonnavn().getMellomnavn() != null;
    }

    private static String finnEtterNavn(XMLBruker xmlBruker) {
        return etternavnExists(xmlBruker) ? xmlBruker.getPersonnavn().getEtternavn() : "";
    }

    private static boolean etternavnExists(XMLBruker xmlBruker) {
        return xmlBruker.getPersonnavn() != null && xmlBruker.getPersonnavn().getEtternavn() != null;
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest lagXMLRequestPreferanser(String ident) {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(ident);
    }

}
