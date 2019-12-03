package no.nav.sbl.dialogarena.soknadinnsending.consumer.person.mappers;

import com.google.common.collect.ImmutableMap;
import no.ks.svarut.servicesv9.PostAdresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.NavFodselsnummer;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonMapper;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Bostedsadresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Gateadresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Matrikkeladresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.PostboksadresseNorsk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.StrukturertAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.UstrukturertAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.*;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import org.joda.time.LocalDate;

import java.util.*;

import static java.util.Optional.ofNullable;

public class PersonDataMapper {
    static final String RELASJON_EKTEFELLE = "EKTE";
    static final String RELASJON_REGISTRERT_PARTNER = "REPA";
    static final String RELASJON_BARN = "BARN";
    static final String KODE_6 = "SPSF";
    static final String KODE_7 = "SPFO";
    static final String KODE_6_TALLFORM = "6";
    static final String KODE_7_TALLFORM = "7";
    static final String DOED = "DØD";
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

    public static String finnSivilstatus(PersonData personData) {
        if (personData.getSivilStand() == null ) {
            return null;
        }
        return MAP_XMLSIVILSTATUS_TIL_JSONSIVILSTATUS.get(personData.getSivilStand().getSivilstand().getValue());
    }

    public static List<Barn> finnBarnForPerson(Person xmlPerson) {
        final List<Familierelasjon> familierelasjoner = finnFamilierelasjonerForPerson(xmlPerson);
        List<Barn> alleBarn = new ArrayList<>();
        for (Familierelasjon familierelasjon : familierelasjoner) {
            Familierelasjoner familierelasjonType = familierelasjon.getTilRolle();
            if (RELASJON_BARN.equals(familierelasjonType.getValue())) {
                alleBarn.add(mapFamilierelasjonTilBarn(familierelasjon));
            }
        }
        alleBarn.removeIf(Objects::isNull);
        return alleBarn;
    }

    public static List<Familierelasjon> finnFamilierelasjonerForPerson(Person xmlPerson) {
        List<Familierelasjon> familierelasjoner = xmlPerson.getHarFraRolleI();
        if (familierelasjoner == null || familierelasjoner.isEmpty()) {
            return new ArrayList<>();
        }
        return familierelasjoner;
    }

    private static Barn mapFamilierelasjonTilBarn(Familierelasjon familierelasjon) {
        Person barn = familierelasjon.getTilPerson();
        if (xmlPersonHarDiskresjonskode(barn)) {
            return null;
        }

        if (!PersonMapper.erMyndig(finnFodselsdatoFraFnr(barn)) && !PersonMapper.erDoed(barn)) {
            return new Barn()
                    .withFornavn(finnFornavn(barn))
                    .withMellomnavn(finnMellomnavn(barn))
                    .withEtternavn(finnEtternavn(barn))
                    .withFnr(finnFodselsnummer(barn))
                    .withFodselsdato(finnFodselsdatoFraFnr(barn))
                    .withFolkeregistrertsammen(familierelasjon.isHarSammeBosted() != null ? familierelasjon.isHarSammeBosted() : false)
                    .withIkkeTilgang(false);
        } else {
            return null;
        }
    }

    public static LocalDate finnFodselsdatoFraFnr(Person xmlPerson) {
        if (xmlPerson.getFoedselsdato()== null) {
            return null;
        }
            String fnr = PersonMapper.finnFnr(xmlPerson);
        if (fnr != null) {
            NavFodselsnummer navFnr = new NavFodselsnummer(fnr);

            return new LocalDate(navFnr.getBirthYear() + "-" + navFnr.getMonth() + "-" + navFnr.getDayInMonth());
        }
        return null;
    }

    public static String finnFornavn(Person xmlPerson) {
        return xmlPerson.getPersonnavn() != null && xmlPerson.getPersonnavn().getFornavn() != null ? xmlPerson.getPersonnavn().getFornavn() : "";
    }

    public static boolean xmlPersonHarDiskresjonskode(Person xmlPerson) {
        final String diskresjonskode = PersonMapper.finnDiskresjonskode(xmlPerson);
        return KODE_6_TALLFORM.equalsIgnoreCase(diskresjonskode) || KODE_6.equalsIgnoreCase(diskresjonskode)
                || KODE_7_TALLFORM.equalsIgnoreCase(diskresjonskode) || KODE_7.equalsIgnoreCase(diskresjonskode);
    }


    public PersonData tilPersonData(Person person) {
        return new PersonData()
                .withFornavn(kanskjeFornavn(person))
                .withMellomnavn(finnMellomnavn(person))
                .withEtternavn(finnEtternavn(person))
                .withDiskresjonskode(kanskjeDiskresjonskode(person))
                .withStatsborgerskap(finnStatsborgerskap(person))
                .withKontonummer(kanskjeKontonummer(person))
                .withBostedsadresse(finnBostedsadresse(person))
                .withMidlertidigAdresseNorge(finnMidlertidigAdresseNorge(person))
                .withMidlertidigAdresseUtland(finnMidlertidigAdresseUtland(person))
                .withPostAdresse(finnPostAdresse(person));
    }

    private static String finnFodselsnummer(Person person) {
        Aktoer aktoer = person.getAktoer();
        if (aktoer instanceof PersonIdent) {
            return finnNorskIdent((PersonIdent) aktoer);
        }
        return null;
    }

    private static String finnNorskIdent(PersonIdent aktoer) {
        return ofNullable(aktoer.getIdent())
                .map(NorskIdent::getIdent)
                .orElse(null);
    }

    private static String finnEtternavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getEtternavn)
                .orElse(null);
    }

    private String kanskjeFornavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getFornavn)
                .orElse(null);
    }

    private static String finnStatsborgerskap(Person person) {
        return ofNullable(person.getStatsborgerskap())
                .map(Statsborgerskap::getLand)
                .map(Kodeverdi::getValue)
                .orElse(null);
    }

    public static String finnMellomnavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getMellomnavn)
                .orElse(null);
    }

    private static Bostedsadresse finnBostedsadresse(Person person) {
        Bostedsadresse bostedsadresse = null;

        no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse wsBostedsadresse = person.getBostedsadresse();
        if (wsBostedsadresse != null) {
            bostedsadresse = new Bostedsadresse();
            bostedsadresse.withStrukturertAdresse(mapStrukturertAdresse(wsBostedsadresse.getStrukturertAdresse()));
        }
        return bostedsadresse;
    }

    private static MidlertidigAdresseNorge finnMidlertidigAdresseNorge(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        MidlertidigAdresseNorge midlertidigAdresseNorge = null;

        if (person instanceof Bruker) {
            MidlertidigPostadresse wsMidlertidigPostadresse = ((Bruker) person).getMidlertidigPostadresse();
            if (wsMidlertidigPostadresse instanceof MidlertidigPostadresseNorge) {
                midlertidigAdresseNorge = new MidlertidigAdresseNorge();
                midlertidigAdresseNorge.withStrukturertAdresse(mapStrukturertAdresse(
                        ((MidlertidigPostadresseNorge) wsMidlertidigPostadresse).getStrukturertAdresse()));
            }
        }
        return midlertidigAdresseNorge;
    }

    private static MidlertidigAdresseUtland finnMidlertidigAdresseUtland(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        MidlertidigAdresseUtland midlertidigAdresseUtland = null;

        if (person instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresse wsMidlertidigPostadresse =
                    ((no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) person).getMidlertidigPostadresse();
            if (wsMidlertidigPostadresse instanceof MidlertidigPostadresseUtland) {
                midlertidigAdresseUtland = new MidlertidigAdresseUtland();
                midlertidigAdresseUtland.withUstrukturertAdresse(tilUstrukturertAdresse(
                        ((no.nav.tjeneste.virksomhet.person.v3.informasjon.MidlertidigPostadresseUtland) wsMidlertidigPostadresse).getUstrukturertAdresse()
                ));
            }
        }
        return midlertidigAdresseUtland;
    }

    private static PostAdresse finnPostAdresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        PostAdresse postAdresse = null;

        no.nav.tjeneste.virksomhet.person.v3.informasjon.Postadresse wsPostadresse = person.getPostadresse();
        if (wsPostadresse != null) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse wsUstrukturertAdresse = wsPostadresse.getUstrukturertAdresse();
            postAdresse = new PostAdresse();
            postAdresse.setAdresse1(ofNullable(wsUstrukturertAdresse.getAdresselinje1())
                    .orElse(null));
            postAdresse.setAdresse2(ofNullable(wsUstrukturertAdresse.getAdresselinje2())
                    .orElse(null));
            postAdresse.setAdresse3(ofNullable(wsUstrukturertAdresse.getAdresselinje3())
                    .orElse(null));
            postAdresse.setLand(ofNullable(wsUstrukturertAdresse.getLandkode().getValue())
                    .orElse(null));
            postAdresse.setPostnr(ofNullable(wsUstrukturertAdresse.getPostnr())
                    .orElse(null));
            postAdresse.setPoststed(ofNullable(wsUstrukturertAdresse.getPoststed())
                    .orElse(null));
        }

        return postAdresse;
    }

    private static StrukturertAdresse mapStrukturertAdresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.StrukturertAdresse wsStrukturertadresse) {
        StrukturertAdresse strukturertAdresse = null;
        if (wsStrukturertadresse instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse) {
            strukturertAdresse = tilGateAdresse((no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse) wsStrukturertadresse);
        } else if (wsStrukturertadresse instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk) {
            strukturertAdresse = tilPostboksadresseNorsk((no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk) wsStrukturertadresse);
        } else if (wsStrukturertadresse instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse) {
            strukturertAdresse = tilMatrikkeladresse((no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse) wsStrukturertadresse);
        }

        if (wsStrukturertadresse.getLandkode() != null) {
            if (strukturertAdresse == null) {
                strukturertAdresse = new StrukturertAdresse();
            }
            strukturertAdresse.withLandkode(wsStrukturertadresse.getLandkode().getValue());
        }
        if (wsStrukturertadresse.getTilleggsadresse() != null) {
            if (strukturertAdresse == null) {
                strukturertAdresse = new StrukturertAdresse();
            }
            strukturertAdresse.withTilleggsadresse(wsStrukturertadresse.getTilleggsadresse());
        }
        return strukturertAdresse;
    }

    private static StrukturertAdresse tilMatrikkeladresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.Matrikkeladresse wsMatrikkeladresse) {
        Optional<Matrikkelnummer> kanskjeMatrikkelnummer = ofNullable(wsMatrikkeladresse.getMatrikkelnummer());
        return new Matrikkeladresse()
                .withKommunenummer(ofNullable(wsMatrikkeladresse.getKommunenummer())
                        .orElse(null))
                .withEiendomsnavn(ofNullable(wsMatrikkeladresse.getEiendomsnavn())
                        .orElse(null))
                .withGardsnummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getGaardsnummer)
                        .orElse(null))
                .withBruksnummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getBruksnummer)
                        .orElse(null))
                .withFestenummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getFestenummer)
                        .orElse(null))
                .withSeksjonsnummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getSeksjonsnummer)
                        .orElse(null))
                .withUndernummer(kanskjeMatrikkelnummer
                        .map(Matrikkelnummer::getUndernummer)
                        .orElse(null))
                .withPostnummer(ofNullable(wsMatrikkeladresse.getPoststed().getValue())
                        .orElse(null));
    }

    private static PostboksadresseNorsk tilPostboksadresseNorsk(no.nav.tjeneste.virksomhet.person.v3.informasjon.PostboksadresseNorsk wsPostboksadresseNorsk) {
        return new PostboksadresseNorsk()
                .withPostnummer(ofNullable(wsPostboksadresseNorsk.getPoststed().getValue()).orElse(null))
                .withPostboksanlegg(ofNullable(wsPostboksadresseNorsk.getPostboksanlegg()).orElse(null))
                .withPostboksnummer(ofNullable(wsPostboksadresseNorsk.getPostboksnummer()).orElse(null));
    }

    private static StrukturertAdresse tilGateAdresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.Gateadresse wsGateadresse) {
        return new Gateadresse()
                .withGatenavn(ofNullable(wsGateadresse.getGatenavn())
                        .orElse(null))
                .withHusnummer(ofNullable(wsGateadresse.getHusnummer())
                        .orElse(null))
                .withHusbokstav(ofNullable(wsGateadresse.getHusbokstav())
                        .orElse(null))
                .withGatenummer(ofNullable(wsGateadresse.getGatenummer())
                        .orElse(null))
                .withKommunenummer(ofNullable(wsGateadresse.getKommunenummer())
                        .orElse(null))
                .withPostnummer(ofNullable(wsGateadresse.getPoststed().getValue())
                        .orElse(null));
    }

    private static UstrukturertAdresse tilUstrukturertAdresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.UstrukturertAdresse wsUstrukturertAdresse) {
        return new UstrukturertAdresse()
                .withAdresselinje1(ofNullable(wsUstrukturertAdresse.getAdresselinje1())
                        .orElse(null))
                .withAdresselinje2(ofNullable(wsUstrukturertAdresse.getAdresselinje2())
                        .orElse(null))
                .withAdresselinje3(ofNullable(wsUstrukturertAdresse.getAdresselinje3())
                        .orElse(null))
                .withAdresselinje4(ofNullable(wsUstrukturertAdresse.getAdresselinje4())
                        .orElse(null))
                .withLandkode(ofNullable(wsUstrukturertAdresse.getLandkode().getValue())
                        .orElse(null));
    }

    private static String kanskjeKontonummer(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        if (person instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkonto bankkonto =
                    ((no.nav.tjeneste.virksomhet.person.v3.informasjon.Bruker) person).getBankkonto();
            return kanskjeKontonummer(bankkonto);
        }
        return null;
    }

    private static String kanskjeKontonummer(no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkonto bankkonto) {
        if (bankkonto instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge bankkontoNorge =
                    (no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoNorge) bankkonto;
            return ofNullable(bankkontoNorge.getBankkonto())
                    .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Bankkontonummer::getBankkontonummer)
                    .orElse(null);
        } else if (bankkonto instanceof no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland) {
            no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland wsBankkontoUtland =
                    (no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontoUtland) bankkonto;
            return ofNullable(wsBankkontoUtland.getBankkontoUtland())
                    .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.BankkontonummerUtland::getBankkontonummer)
                    .orElse(null);
        }
        return null;
    }

    private static String kanskjeDiskresjonskode(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
        return ofNullable(person.getDiskresjonskode())
                .map(no.nav.tjeneste.virksomhet.person.v3.informasjon.Kodeverdi::getValue)
                .map(DiskresjonskodeMapper::mapTilTallkode)
                .orElse(null);
    }
}