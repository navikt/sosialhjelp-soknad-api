package no.nav.sbl.dialogarena.soknadinnsending.consumer.kontaktinfo;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.AdresserOgKontonummer;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.ApplicationException;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.StatsborgerskapType;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.AdresseTransform;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.IkkeFunnetException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.TjenesteUtilgjengeligException;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkonto;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Adressetype.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class BrukerprofilService {
    private static final Logger logger = getLogger(BrukerprofilService.class);

    @Inject
    @Named("brukerProfilEndpoint")
    private BrukerprofilPortType brukerProfil;

    @Inject
    private Kodeverk kodeverk;

    @Cacheable("adresserOgKontonummerCache")
    public AdresserOgKontonummer hentKontaktinformasjonOgPreferanser(String fodselsnummer) {
        try {
            return mapResponsTilAdresserOgKontonummer(brukerProfil.hentKontaktinformasjonOgPreferanser(lagXMLRequestPreferanser(fodselsnummer)));
        } catch (HentKontaktinformasjonOgPreferanserSikkerhetsbegrensning e) {
            logger.warn("Kunne ikke hente bruker fra TPS på grunn av manglende tilgang.", e);
            return new AdresserOgKontonummer()
                    .withFolkeregistrertAdresse(new Adresse())
                    .withGjeldendeAdresse(new Adresse())
                    .withSekundarAdresse(new Adresse());
        } catch (HentKontaktinformasjonOgPreferanserPersonIkkeFunnet e) {
            logger.error("Ikke funnet person i TPS", e);
            throw new IkkeFunnetException("TPS:PersonIkkefunnet", e);
        } catch (WebServiceException e) {
            logger.warn("Ingen kontakt med TPS (Brukerprofil_v1).", e);
            throw new TjenesteUtilgjengeligException("TPS:webserviceException", e);
        }
    }

    public AdresserOgKontonummer hentAddresserOgKontonummer(String fodselsnummer) {
        AdresserOgKontonummer adresserOgKontonummer;
        try {
            adresserOgKontonummer = hentKontaktinformasjonOgPreferanser(fodselsnummer);
        } catch (IkkeFunnetException e) {
            logger.error("Ikke funnet person i TPS", e);
            throw new ApplicationException("TPS:PersonIkkefunnet", e);
        }
        return adresserOgKontonummer;
    }

    AdresserOgKontonummer mapResponsTilAdresserOgKontonummer(XMLHentKontaktinformasjonOgPreferanserResponse response) {
        if (response == null || response.getPerson() == null) {
            return null;
        }
        XMLBruker xmlBruker = (XMLBruker) response.getPerson();
        Adresse gjeldendeAdresse = finnGjeldendeAdresse(xmlBruker);
        return new AdresserOgKontonummer()
                .withGjeldendeAdresse(gjeldendeAdresse)
                .withMidlertidigAdresse(finnMidlertidigAdresse(xmlBruker))
                .withFolkeregistrertAdresse(finnFolkeregistrertAdresse(xmlBruker))
                .withSekundarAdresse(finnSekundarAdresse(xmlBruker))
                .withUtenlandskAdresse(harUtenlandskAdresse(gjeldendeAdresse))
                .withBosattIEOSLand(erBosattIEOSLand(gjeldendeAdresse))
                .withKontonummer(finnKontonummer(xmlBruker))
                .withUtenlandskBankkonto(erUtenlandskKonto(xmlBruker))
                .withUtenlandskKontoBanknavn(finnUtenlandsKontoNavn(xmlBruker))
                .withUtenlandskKontoLand(finnUtenlandskKontoLand(xmlBruker));
    }

    Adresse finnMidlertidigAdresse(XMLBruker xmlBruker) {
        return new AdresseTransform().mapMidlertidigAdresse(xmlBruker, kodeverk);
    }

    Adresse finnGjeldendeAdresse(XMLBruker xmlBruker) {
        return new AdresseTransform().mapGjeldendeAdresse(xmlBruker, kodeverk);
    }

    private Adresse finnFolkeregistrertAdresse(XMLBruker xmlBruker) {
        return new AdresseTransform().mapFolkeregistrertAdresse(xmlBruker, kodeverk);
    }

    private Adresse finnSekundarAdresse(XMLBruker xmlBruker) {
        return new AdresseTransform().mapSekundarAdresse(xmlBruker, kodeverk);
    }

    private String finnUtenlandskKontoLand(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();
        if (bankkonto == null || bankkonto instanceof XMLBankkontoNorge) {
            return "";
        }
        String landkode = ((XMLBankkontoUtland) bankkonto).getBankkontoUtland().getLandkode().getValue();
        return kodeverk.getLand(landkode);
    }

    private String finnUtenlandsKontoNavn(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();
        if (bankkonto == null || bankkonto instanceof XMLBankkontoNorge) {
            return "";
        }
        return ((XMLBankkontoUtland) bankkonto).getBankkontoUtland().getBanknavn();
    }

    private Boolean erUtenlandskKonto(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();
        return bankkonto instanceof XMLBankkontoUtland;
    }

    private String finnKontonummer(XMLBruker xmlBruker) {
        XMLBankkonto bankkonto = xmlBruker.getBankkonto();
        if (bankkonto instanceof XMLBankkontoUtland) {
            return ((XMLBankkontoUtland) bankkonto).getBankkontoUtland().getBankkontonummer();
        } else if (bankkonto instanceof XMLBankkontoNorge) {
            return ((XMLBankkontoNorge) bankkonto).getBankkonto().getBankkontonummer();
        } else {
            return "";
        }
    }

    private XMLHentKontaktinformasjonOgPreferanserRequest lagXMLRequestPreferanser(String ident) {
        return new XMLHentKontaktinformasjonOgPreferanserRequest().withIdent(ident);
    }

    private boolean erBosattIEOSLand(Adresse gjeldendeAdresse) {
        return !harUtenlandskAdresse(gjeldendeAdresse) || harUtenlandskAdresseIEOS(gjeldendeAdresse);
    }

    private boolean harUtenlandskAdresseIEOS(Adresse gjeldendeAdresse) {
        String adressetype = null;
        String landkode = null;
        if (gjeldendeAdresse != null) {
            adressetype = gjeldendeAdresse.getAdressetype();
            landkode = gjeldendeAdresse.getLandkode();
        }

        if (adressetype == null) {
            return false;
        }

        return (harUtenlandsAdressekode(adressetype)) && (StatsborgerskapType.get(landkode).equals(EOS));
    }

    private boolean harUtenlandsAdressekode(String adressetype) {
        return adressetype.equalsIgnoreCase(MIDLERTIDIG_POSTADRESSE_UTLAND.name()) ||
                adressetype.equalsIgnoreCase(POSTADRESSE_UTLAND.name()) ||
                adressetype.equalsIgnoreCase(UTENLANDSK_ADRESSE.name());
    }

    private boolean harUtenlandskAdresse(Adresse gjeldendeAdresse) {
        String adressetype = null;

        if (gjeldendeAdresse != null) {
            adressetype = gjeldendeAdresse.getAdressetype();
        }

        if (adressetype == null) {
            return false;
        }

        return harUtenlandsAdressekode(adressetype);
    }
}
