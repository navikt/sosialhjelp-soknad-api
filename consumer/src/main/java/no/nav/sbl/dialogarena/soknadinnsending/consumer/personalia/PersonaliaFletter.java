package no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia;

import com.google.common.collect.ImmutableMap;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.*;
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
import no.nav.tjeneste.virksomhet.person.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Denne klassen fletter sammen data fra 2 forskjellige endepunkter for å populere
 * domeneobjektet
 */
@Component
public class PersonaliaFletter {

    static final String RELASJON_EKTEFELLE = "EKTE";
    static final String RELASJON_REGISTRERT_PARTNER = "REPA";
    static final String KODE_6 = "SPSF";
    static final String KODE_7 = "SPFO";
    static final String KODE_6_TALLFORM = "6";
    static final String KODE_7_TALLFORM = "7";
    static final String UTVANDRET = "UTVA";
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
    private static final Map<String, String> MAP_XMLSIVILSTATUS_TIL_JSONSIVILSTATUS = new ImmutableMap.Builder<String, String>()
            .put("GIFT", "gift")
            .put("GLAD", "gift")
            .put("REPA", "gift")
            .put("SAMB", "samboer")
            .put("UGIF", "ugift")
            .put("ENKE", "enke")
            .put("GJPA", "enke")
            .put("SEPA", "separert")
            .put("SEPR", "separert")
            .put("SKIL", "skilt")
            .put("SKPA", "skilt").build();


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
                .withFornavn(finnFornavn(xmlBruker).trim())
                .withMellomnavn(finnMellomNavn(xmlBruker).trim())
                .withEtternavn(finnEtterNavn(xmlBruker))
                .epost(finnEpost(dkifResponse))
                .mobiltelefon(finnMobiltelefonnummer(dkifResponse))
                .statsborgerskap(finnStatsborgerskap(xmlPerson))
                .kjonn(finnKjonn(xmlBruker))
                .gjeldendeAdresse(finnGjeldendeAdresse(xmlBruker, kodeverk))
                .sekundarAdresse(finnSekundarAdresse(xmlBruker, kodeverk))
                .folkeregistrertAdresse(finnFolkeregistrertAdresse(xmlBruker, kodeverk))
                .kontonummer(finnKontonummer(xmlBruker))
                .erUtenlandskBankkonto(erUtenlandskKonto(xmlBruker))
                .utenlandskKontoBanknavn(finnUtenlandsKontoNavn(xmlBruker))
                .utenlandskKontoLand(finnUtenlandskKontoLand(xmlBruker, kodeverk))
                .sivilstatus(finnSivilstatus(xmlPerson))
                .ektefelle(finnEktefelle(xmlPerson))
                .build();
    }

    private String finnSivilstatus(Person xmlPerson) {
        if (xmlPerson.getSivilstand() == null || xmlPerson.getSivilstand().getSivilstand() == null) {
            return null;
        }
        return MAP_XMLSIVILSTATUS_TIL_JSONSIVILSTATUS.get(xmlPerson.getSivilstand().getSivilstand().getValue());
    }

    Ektefelle finnEktefelle(Person xmlPerson) {
        List<Familierelasjon> familierelasjoner = xmlPerson.getHarFraRolleI();
        if (familierelasjoner.isEmpty()) {
            return null;
        }
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            if (RELASJON_EKTEFELLE.equals(familierelasjonType.getValue()) || RELASJON_REGISTRERT_PARTNER.equals(familierelasjonType.getValue())) {
                Person xmlEktefelle = familierelasjon.getTilPerson();
                if (ektefelleHarDiskresjonskode(xmlEktefelle)) {
                    return new Ektefelle()
                            .withIkketilgangtilektefelle(true);
                }
                boolean ektefelleErUtvandret = ektefelleErUtvandret(xmlEktefelle);
                return new Ektefelle()
                        .withFornavn(xmlEktefelle.getPersonnavn() != null ? xmlEktefelle.getPersonnavn().getFornavn() : null)
                        .withMellomnavn(xmlEktefelle.getPersonnavn() != null ? xmlEktefelle.getPersonnavn().getMellomnavn() : null)
                        .withEtternavn(xmlEktefelle.getPersonnavn() != null ? xmlEktefelle.getPersonnavn().getEtternavn() : null)
                        .withFodselsdato(finnFodselsdatoForEktefelle(xmlEktefelle))
                        .withFnr(xmlEktefelle.getIdent() != null ? xmlEktefelle.getIdent().getIdent() : null)
                        .withFolkeregistrertsammen(ektefelleErUtvandret ? false : familierelasjon.isHarSammeBosted())
                        .withIkketilgangtilektefelle(false);
            }
        }
        return null;
    }

    boolean ektefelleHarDiskresjonskode(Person xmlEktefelle) {
        if (xmlEktefelle.getDiskresjonskode() == null) {
            return false;
        }
        final String diskresjonskode = xmlEktefelle.getDiskresjonskode().getValue();
        return KODE_6_TALLFORM.equalsIgnoreCase(diskresjonskode) || KODE_6.equalsIgnoreCase(diskresjonskode)
                || KODE_7_TALLFORM.equalsIgnoreCase(diskresjonskode) || KODE_7.equalsIgnoreCase(diskresjonskode);
    }

    private boolean ektefelleErUtvandret(Person xmlEktefelle) {
        if (xmlEktefelle.getPersonstatus() == null || xmlEktefelle.getPersonstatus().getPersonstatus() == null) {
            return false;
        }
        return UTVANDRET.equalsIgnoreCase(xmlEktefelle.getPersonstatus().getPersonstatus().getValue());
    }

    private LocalDate finnFodselsdatoForEktefelle(Person ektefelle) {
        if (ektefelle.getIdent() == null || ektefelle.getIdent().getType() == null) {
            return null;
        }
        String identtype = ektefelle.getIdent().getType().getValue();
        String ident = ektefelle.getIdent().getIdent();
        if ("FNR".equalsIgnoreCase(identtype) && isNotEmpty(ident)) {
            NavFodselsnummer fnr = new NavFodselsnummer(ektefelle.getIdent().getIdent());
            return new LocalDate(fnr.getBirthYear() + "-" + fnr.getMonth() + "-" + fnr.getDayInMonth());
        }
        return null;
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

    private static String finnStatsborgerskap(Person xmlPerson) {
        if (xmlPerson.getStatsborgerskap() != null) {
            Statsborgerskap statsborgerskap = xmlPerson.getStatsborgerskap();
            return statsborgerskap.getLand().getValue();
        } else {
            return "NOR";
        }
    }

    private static LocalDate finnFodselsdato(Person person) {
        if (person.getFoedselsdato() == null || person.getFoedselsdato().getFoedselsdato() == null) {
            return null;
        }
        return new LocalDate(person.getFoedselsdato().getFoedselsdato().toGregorianCalendar());
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
