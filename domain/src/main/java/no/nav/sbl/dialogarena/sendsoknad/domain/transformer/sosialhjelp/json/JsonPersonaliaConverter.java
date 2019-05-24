package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn.Kilde;

import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.*;

public final class JsonPersonaliaConverter {

    private JsonPersonaliaConverter() {

    }

    public static JsonPersonalia tilPersonalia(WebSoknad webSoknad) {
        final JsonPersonalia personalia = new JsonPersonalia();

        final Map<String, String> personaliaProperties = webSoknad.getFaktumMedKey("personalia").getProperties();

        personalia.setPersonIdentifikator(new JsonPersonIdentifikator().withVerdi(webSoknad.getAktoerId()));
        personalia.setNavn(new JsonSokernavn()
                .withKilde(Kilde.SYSTEM)
                .withFornavn(JsonUtils.finnPropertyEllerTom(personaliaProperties, FORNAVN_KEY))
                .withMellomnavn(JsonUtils.finnPropertyEllerTom(personaliaProperties, MELLOMNAVN_KEY))
                .withEtternavn(JsonUtils.finnPropertyEllerTom(personaliaProperties, ETTERNAVN_KEY))
        );

        final String statsborgerskap = personaliaProperties.get(STATSBORGERSKAP_KEY);
        if (erIkkeTom(statsborgerskap) && !statsborgerskap.equals("???")) {
            personalia.setStatsborgerskap(new JsonStatsborgerskap()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(statsborgerskap));
            personalia.setNordiskBorger(new JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(erNordiskBorger(statsborgerskap)));
        } else {
            final String nordiskBorger = webSoknad.getValueForFaktum("kontakt.statsborger");
            if (erIkkeTom(nordiskBorger)) {
                personalia.setNordiskBorger(new JsonNordiskBorger()
                        .withKilde(JsonKilde.BRUKER)
                        .withVerdi(Boolean.parseBoolean(nordiskBorger)));
            }
        }

        personalia.setTelefonnummer(tilJsonTelefonnummer(webSoknad));
        personalia.setKontonummer(tilJsonKontonummer(webSoknad));

        personalia.setFolkeregistrertAdresse(JsonAdresseConverter.tilFolkeregistrertAdresse(webSoknad));
        personalia.setOppholdsadresse(JsonAdresseConverter.tilOppholdsadresse(webSoknad));
        personalia.setPostadresse(JsonAdresseConverter.tilPostadresse(webSoknad));

        if(erIkkeTom(webSoknad.getValueForFaktum("kontakt.system.oppholdsadresse.valg")) && personalia.getOppholdsadresse() != null) {
            personalia.getOppholdsadresse().setAdresseValg(
                    JsonAdresseValg.fromValue(webSoknad.getValueForFaktum("kontakt.system.oppholdsadresse.valg")));
        }

        return personalia;
    }

    private static boolean erNordiskBorger(String statsborgerskap) {
        
        /* TODO: Ligger denne logikken et annet sted? Hvor bør dette legges? */
        switch (statsborgerskap) {
            case "NOR":
            case "SWE":
            case "FRO":
            case "ISL":
            case "DNK":
            case "FIN":
                return true;
            default:
                return false;
        }
    }

    private static JsonTelefonnummer tilJsonTelefonnummer(WebSoknad webSoknad) {
        if (faktumVerdiErTrue(webSoknad, "kontakt.telefon.brukerendrettoggle")) {
            return tilBrukerJsonTelefonnummer(webSoknad);
        } else {
            return tilSystemJsonTelefonnummer(webSoknad);
        }
    }

    private static JsonTelefonnummer tilBrukerJsonTelefonnummer(WebSoknad webSoknad) {
        final String landkode = "+47";
        final String telefonnummer = webSoknad.getValueForFaktum("kontakt.telefon");
        if (erIkkeTom(telefonnummer)) {
            final JsonTelefonnummer jsonTelefonnummer = new JsonTelefonnummer();
            jsonTelefonnummer.setVerdi(landkode + telefonnummer);
            jsonTelefonnummer.setKilde(JsonKilde.BRUKER);
            return jsonTelefonnummer;
        } else {
            return null;
        }
    }

    private static JsonTelefonnummer tilSystemJsonTelefonnummer(WebSoknad webSoknad) {
        final String telefonnummer = webSoknad.getValueForFaktum("kontakt.system.telefon");
        if (erIkkeTom(telefonnummer)) {
            final JsonTelefonnummer jsonTelefonnummer = new JsonTelefonnummer();
            jsonTelefonnummer.setVerdi(telefonnummer);
            jsonTelefonnummer.setKilde(JsonKilde.SYSTEM);
            return jsonTelefonnummer;
        } else {
            return null;
        }
    }

    private static JsonKontonummer tilJsonKontonummer(WebSoknad webSoknad) {
        final JsonKontonummer jsonKontonummer = new JsonKontonummer();
        if (faktumVerdiErTrue(webSoknad, "kontakt.kontonummer.brukerendrettoggle")) {
            tilBrukerJsonKontonummer(webSoknad, jsonKontonummer);
        } else {
            tilSystemJsonKontonummer(webSoknad, jsonKontonummer);
        }

        return jsonKontonummer;
    }

    private static void tilBrukerJsonKontonummer(WebSoknad webSoknad, final JsonKontonummer jsonKontonummer) {
        jsonKontonummer.setKilde(JsonKilde.BRUKER);

        final String harIkkeKontonummer = webSoknad.getValueForFaktum("kontakt.kontonummer.harikke");
        if (erIkkeTom(harIkkeKontonummer)) {
            jsonKontonummer.setHarIkkeKonto(Boolean.valueOf(harIkkeKontonummer));
        }

        final String kontonummer = webSoknad.getValueForFaktum("kontakt.kontonummer");
        if (erIkkeTom(kontonummer)) {
            String kontonummerKunTall = kontonummer.replace(".", "").replace(" ", "");
            jsonKontonummer.setVerdi(kontonummerKunTall);
        }
    }

    private static void tilSystemJsonKontonummer(WebSoknad webSoknad, final JsonKontonummer jsonKontonummer) {
        jsonKontonummer.setKilde(JsonKilde.SYSTEM);

        final String kontonummer = webSoknad.getValueForFaktum("kontakt.system.kontonummer");
        if (erIkkeTom(kontonummer)) {
            jsonKontonummer.setVerdi(kontonummer);
        } else {
            jsonKontonummer.setKilde(JsonKilde.BRUKER);
        }
    }
}
