package no.nav.sbl.dialogarena.soknadinnsending.consumer.person.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.ks.svarut.servicesv9.PostAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Bostedsadresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Gateadresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.Matrikkeladresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.PostboksadresseNorsk;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.StrukturertAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.UstrukturertAdresse;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain.*;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.*;
import org.slf4j.Logger;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

public class PersonDataMapper {

    private static final String KODE_6 = "6";
    private static final String KODE_7 = "7";

    private static final Logger log = getLogger(PersonService.class);
    public PersonData tilPersonData(Person person) {
        try {
            String s = JsonSosialhjelpObjectMapper.createObjectMapper().writeValueAsString(person);
            log.warn(s);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return new PersonData()
                .withFornavn(kanskjeFornavn(person))
                .withMellomnavn(kanskjeMellomnavn(person))
                .withEtternavn(kanskjeEtternavn(person))
                .withDiskresjonskode(kanskjeDiskresjonskode(person))
                .withKontonummer(kanskjeKontonummer(person))
                .withBostedsadresse(kanskjeBostedsadresse(person))
                .withMidlertidigAdresseNorge(kanskjeMidlertidigAdresseNorge(person))
                .withMidlertidigAdresseUtland(kanskjeMidlertidigAdresseUtland(person))
                .withPostAdresse(kanskjePostAdresse(person));
    }

    private static String kanskjeFodselsnummer(Person person) {
        Aktoer aktoer = person.getAktoer();
        if (aktoer instanceof PersonIdent) {
            return kanskjeNorskIdent((PersonIdent) aktoer);
        }
        return null;
    }

    private static String kanskjeNorskIdent(PersonIdent aktoer) {
        return ofNullable(aktoer.getIdent())
                .map(NorskIdent::getIdent)
                .orElse(null);
    }

    private String kanskjeEtternavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getEtternavn)
                .orElse(null);
    }

    private String kanskjeFornavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getFornavn)
                .orElse(null);
    }

    private String kanskjeMellomnavn(Person person) {
        return ofNullable(person.getPersonnavn())
                .map(Personnavn::getMellomnavn)
                .orElse(null);
    }

    private static Bostedsadresse kanskjeBostedsadresse(Person person) {
        Bostedsadresse bostedsadresse = null;

        no.nav.tjeneste.virksomhet.person.v3.informasjon.Bostedsadresse wsBostedsadresse = person.getBostedsadresse();
        if (wsBostedsadresse != null) {
            bostedsadresse = new Bostedsadresse();
            bostedsadresse.withStrukturertAdresse(mapStrukturertAdresse(wsBostedsadresse.getStrukturertAdresse()));
        }
        return bostedsadresse;
    }

    private static MidlertidigAdresseNorge kanskjeMidlertidigAdresseNorge(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
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

    private static MidlertidigAdresseUtland kanskjeMidlertidigAdresseUtland(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
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

    private static PostAdresse kanskjePostAdresse(no.nav.tjeneste.virksomhet.person.v3.informasjon.Person person) {
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