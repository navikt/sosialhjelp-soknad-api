package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;

import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.*;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.isFaktumVerdi;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.nonEmpty;

public final class JsonPersonaliaConverter {

    private JsonPersonaliaConverter() {

    }

    public static JsonPersonalia toPersonalia(WebSoknad webSoknad) {
        final JsonPersonalia personalia = new JsonPersonalia();

        final Map<String, String> personaliaProperties = webSoknad.getFaktumMedKey("personalia").getProperties();

        personalia.setPersonIdentifikator(new JsonPersonIdentifikator().withVerdi(webSoknad.getAktoerId()));
        personalia.setNavn(new JsonSokernavn()
                .withFornavn(personaliaProperties.get(FORNAVN_KEY))
                .withMellomnavn(personaliaProperties.get(MELLOMNAVN_KEY))
                .withEtternavn(personaliaProperties.get(ETTERNAVN_KEY))
        );

        final String statsborgerskap = personaliaProperties.get(STATSBORGERSKAP_KEY);
        if (nonEmpty(statsborgerskap) && !statsborgerskap.equals("???")) {
            personalia.setStatsborgerskap(new JsonStatsborgerskap()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(statsborgerskap));
            personalia.setNordiskBorger(new JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(isNordiskBorger(statsborgerskap)));
        } else {
            final String nordiskBorger = webSoknad.getValueForFaktum("kontakt.statsborger");
            if (nonEmpty(nordiskBorger)) {
                personalia.setNordiskBorger(new JsonNordiskBorger()
                        .withKilde(JsonKilde.BRUKER)
                        .withVerdi(Boolean.parseBoolean(nordiskBorger)));
            }
        }

        personalia.setTelefonnummer(toJsonTelefonnummer(webSoknad));
        personalia.setKontonummer(toJsonKontonummer(webSoknad));

        personalia.setFolkeregistrertAdresse(JsonAdresseConverter.toFolkeregistrertAdresse(webSoknad));
        personalia.setOppholdsadresse(JsonAdresseConverter.toOppholdsadresse(webSoknad));
        personalia.setPostadresse(JsonAdresseConverter.toPostadresse(webSoknad));

        return personalia;
    }

    private static boolean isNordiskBorger(String statsborgerskap) {
        if (statsborgerskap == null) {
            return false;
        }
        
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

    private static JsonTelefonnummer toJsonTelefonnummer(WebSoknad webSoknad) {
        if (isFaktumVerdi(webSoknad, "kontakt.telefon.brukerendrettoggle")) {
            return toBrukerJsonTelefonnummer(webSoknad);
        } else {
            return toSystemJsonTelefonnummer(webSoknad);
        }
    }

    private static JsonTelefonnummer toBrukerJsonTelefonnummer(WebSoknad webSoknad) {
        final String telefonnummer = webSoknad.getValueForFaktum("kontakt.telefon");
        if (nonEmpty(telefonnummer)) {
            final JsonTelefonnummer jsonTelefonnummer = new JsonTelefonnummer();
            jsonTelefonnummer.setVerdi(telefonnummer);
            jsonTelefonnummer.setKilde(JsonKilde.BRUKER);
            return jsonTelefonnummer;
        } else {
            return null;
        }
    }

    private static JsonTelefonnummer toSystemJsonTelefonnummer(WebSoknad webSoknad) {
        final String telefonnummer = webSoknad.getValueForFaktum("kontakt.system.telefon");
        if (nonEmpty(telefonnummer)) {
            final JsonTelefonnummer jsonTelefonnummer = new JsonTelefonnummer();
            jsonTelefonnummer.setVerdi(telefonnummer);
            jsonTelefonnummer.setKilde(JsonKilde.SYSTEM);
            return jsonTelefonnummer;
        } else {
            return null;
        }
    }

    private static JsonKontonummer toJsonKontonummer(WebSoknad webSoknad) {
        final JsonKontonummer jsonKontonummer = new JsonKontonummer();
        if (isFaktumVerdi(webSoknad, "kontakt.kontonummer.brukerendrettoggle")) {
            toBrukerJsonKontonummer(webSoknad, jsonKontonummer);
        } else {
            toSystemJsonKontonummer(webSoknad, jsonKontonummer);
        }

        return jsonKontonummer;
    }

    private static void toBrukerJsonKontonummer(WebSoknad webSoknad, final JsonKontonummer jsonKontonummer) {
        jsonKontonummer.setKilde(JsonKilde.BRUKER);

        final String harIkkeKontonummer = webSoknad.getValueForFaktum("kontakt.kontonummer.harikke");
        if (nonEmpty(harIkkeKontonummer)) {
            jsonKontonummer.setHarIkkeKonto(Boolean.valueOf(harIkkeKontonummer));
        }

        final String kontonummer = webSoknad.getValueForFaktum("kontakt.kontonummer");
        if (nonEmpty(kontonummer)) {
            jsonKontonummer.setVerdi(kontonummer);
        }
    }

    private static void toSystemJsonKontonummer(WebSoknad webSoknad, final JsonKontonummer jsonKontonummer) {
        jsonKontonummer.setKilde(JsonKilde.SYSTEM);

        final String kontonummer = webSoknad.getValueForFaktum("kontakt.system.kontonummer");
        if (nonEmpty(kontonummer)) {
            jsonKontonummer.setVerdi(kontonummer);
        }
    }
}
