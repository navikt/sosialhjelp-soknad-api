package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.PersonAlder;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkonto;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoNorge;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBankkontoUtland;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLEPost;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLElektroniskKommunikasjonskanal;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v1.informasjon.Statsborgerskap;
import no.nav.tjeneste.virksomhet.person.v1.meldinger.HentKjerneinformasjonResponse;
import org.joda.time.LocalDate;

public class PersonaliaTransform {

    private static final String KJONN_MANN = "m";
    private static final String KJONN_KVINNE = "k";

    public static Personalia mapTilPersonalia(XMLHentKontaktinformasjonOgPreferanserResponse response, HentKjerneinformasjonResponse kjerneinformasjonResponse, Kodeverk kodeverk) {
        if (response == null) {
            return new Personalia();
        }

        XMLBruker xmlBruker = (XMLBruker) response.getPerson();
        Person xmlPerson = kjerneinformasjonResponse.getPerson();
        Diskresjonskoder diskresjonskode = kjerneinformasjonResponse.getPerson().getDiskresjonskode();
        String diskresjonskodeString = diskresjonskode == null ? null : diskresjonskode.getValue();

        return PersonaliaBuilder.
                with()
                .fodselsnummer(finnFnr(xmlBruker))
                .fodselsdato(finnFodselsdato(xmlPerson))
                .alder(finnAlder(finnFnr(xmlBruker)))
                .diskresjonskode(diskresjonskodeString)
                .navn(finnSammensattNavn(xmlBruker))
                .epost(finnEpost(xmlBruker))
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

        if (bankkonto == null  || bankkonto instanceof XMLBankkontoNorge) {
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
        if(xmlPerson.getStatsborgerskap() != null) {
            Statsborgerskap statsborgerskap = xmlPerson.getStatsborgerskap();
            return statsborgerskap.getLand().getValue();
        } else {
            return "NOR";
        }
    }

    private static LocalDate finnFodselsdato(Person person) {
        return new LocalDate(person.getFoedselsdato().getFoedselsdato().toGregorianCalendar());
    }

    private static String finnEpost(XMLBruker xmlBruker) {
        for (XMLElektroniskKommunikasjonskanal kanal : xmlBruker.getElektroniskKommunikasjonskanal()) {
            if (kanal.getElektroniskAdresse() instanceof XMLEPost) {
                return ((XMLEPost) kanal.getElektroniskAdresse()).getIdentifikator();
            }
        }
        return "";
    }

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
}
