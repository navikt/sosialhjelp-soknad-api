package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse.Type;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

public final class JsonAdresseConverter {

    private static final Logger logger = getLogger(JsonAdresseConverter.class);

    private JsonAdresseConverter() {

    }

    public static JsonAdresse tilFolkeregistrertAdresse(WebSoknad webSoknad) {
        try {
            final Faktum faktum = webSoknad.getFaktumMedKey("kontakt.system.folkeregistrert.adresse");
            return tilSystemAdresse(faktum);
        } catch (RuntimeException e) {
            logger.info("Kan ikke legge med folkeregistrert adresse", e);
            return null;
        }
    }

    public static JsonAdresse tilOppholdsadresse(WebSoknad webSoknad) {
        try {
            final String oppholdsadressevalg = webSoknad.getValueForFaktum("kontakt.system.oppholdsadresse.valg");
            if (faktumVerdiErTrue(webSoknad, "kontakt.adresse.brukerendrettoggle")
                    || "soknad".equals(oppholdsadressevalg)) {
                final Faktum faktum = webSoknad.getFaktumMedKey("kontakt.adresse.bruker");
                if (faktum == null) {
                    return null;
                }
                
                if (JsonUtils.erIkkeTom(faktum.getProperties().get("type"))) {
                    return tilBrukerAdresse(faktum);
                } else {
                    return tilBrukersUstrukturertAdresse(faktum);
                }
            } else {
                if ("folkeregistrert".equals(oppholdsadressevalg)) {
                    return tilFolkeregistrertAdresse(webSoknad);
                }
                
                // "midlertidig" eller gammel løsning (null):
                
                final Faktum faktum = webSoknad.getFaktumMedKey("kontakt.system.adresse");
                if (faktum == null) {
                    return null;
                }

                final JsonAdresse adresse = tilSystemAdresse(faktum);
                if (adresse == null || adresse.getType() == Type.POSTBOKS) {
                    return null;
                }
                return adresse;
            }
        } catch (RuntimeException e) {
            logger.error("Uventet feil: Kan ikke sende med oppholdsadresse", e);
            return null;
        }
    }

    public static JsonAdresse tilPostadresse(WebSoknad webSoknad) {
        /* TODO: Implementer støtte for postadresse. */

        final JsonAdresse jsonAdresse = tilOppholdsadresse(webSoknad);
        if (jsonAdresse == null) {
            return null;
        }
        if (jsonAdresse.getType() == Type.MATRIKKELADRESSE) {
            return null;
        }
        return jsonAdresse;
    }

    private static JsonAdresse tilBrukersUstrukturertAdresse(final Faktum faktum) {
        /*
         * TODO: Fjerne denne metoden når grensesnittet er endret til at brukeradresse er strukturert.
         */
        final Map<String, String> adresse = faktum.getProperties();
        final JsonUstrukturertAdresse ustrukturertAdresse = new JsonUstrukturertAdresse();
        ustrukturertAdresse.setType(Type.USTRUKTURERT);
        ustrukturertAdresse.setKilde(JsonKilde.BRUKER);

        final List<String> adresselinjer = new ArrayList<>();
        final String gateadresse = adresse.get("gateadresse");
        if (erIkkeTom(gateadresse)) {
            adresselinjer.add(gateadresse);
        }
        final String postnummer = adresse.get("postnummer");
        final String poststed = adresse.get("poststed");
        String postnummerlinje = "";
        if (erIkkeTom(postnummer)) {
            postnummerlinje += postnummer;
        }
        if (erIkkeTom(poststed)) {
            postnummerlinje += " " + poststed;
        }
        if (!postnummerlinje.trim().equals("")) {
            adresselinjer.add(postnummerlinje);
        }

        ustrukturertAdresse.setAdresse(adresselinjer);
        return ustrukturertAdresse;
    }

    private static JsonAdresse tilBrukerAdresse(Faktum faktum) {
        if (faktum == null) {
            return null;
        }

        try {
            final JsonAdresse adresse = tilJsonAdresse(faktum);
            return adresse;
        } catch (RuntimeException e) {
            logger.error("Uventet feil: Kan ikke sende med brukerspesifisert adresse", e);
            return null;
        }
    }
    
    private static JsonAdresse tilSystemAdresse(Faktum faktum) {
        if (faktum == null) {
            return null;
        }

        try {
            final JsonAdresse adresse = tilJsonAdresse(faktum);
            if (adresse != null && adresse.getKilde() != JsonKilde.SYSTEM) {
                throw new IllegalStateException("Systemadresse skal kun innehold systemdata. Har bruker forsøkt å endre adresse med direkte POST-kall? Faktumkey: " + faktum.getKey());
            }
            return adresse;
        } catch (RuntimeException e) {
            logger.error("Uventet feil: Kan ikke sende med folkeregistrert adresse", e);
            return null;
        }
    }

    private static JsonAdresse tilJsonAdresse(Faktum faktum) {
        final Map<String, String> adresse = faktum.getProperties();

        final String type = adresse.get("type");
        if (type == null) {
            logger.info("Adresse mangler \"type\": " + faktum.getKey());
            return null;
        }

        JsonAdresse jsonAdresse;
        if (type.equals("gateadresse")) {
            jsonAdresse = tilGateAdresse(adresse);
        } else if (type.equals("matrikkeladresse")) {
            jsonAdresse = tilMatrikkelAdresse(adresse);
        } else if (type.equals("ustrukturert")) {
            jsonAdresse = tilUstrukturertAdresse(adresse);
        } else if (type.equals("postboks")) {
            throw new IllegalStateException("Adresser av typen \"postboks\" har ikke blitt implementert ennå.");
        } else {
            throw new IllegalStateException("Ukjent adressetype: \"" + type + "\" for faktum: " + faktum.getKey());
        }

        jsonAdresse.setKilde(erAlleSystemProperties(faktum) ? JsonKilde.SYSTEM : JsonKilde.BRUKER);

        return jsonAdresse;
    }

    private static JsonAdresse tilUstrukturertAdresse(Map<String, String> adresse) {
        final String adresseStreng = finnPropertyEllerNullOmTom(adresse, "adresse");
        if (adresseStreng == null) {
            return null;
        }

        final JsonUstrukturertAdresse ustrukturertAdresse = new JsonUstrukturertAdresse();
        ustrukturertAdresse.setType(Type.USTRUKTURERT);
        // TODO: Bruk linjeskift som hentes fra PersonV1/PersonV3 direkte fremfor å kombinere med komma og deretter splitte: 
        ustrukturertAdresse.setAdresse(Arrays.asList(adresseStreng.split(",")).stream().map(s -> s.trim()).collect(Collectors.toList()));

        return ustrukturertAdresse;
    }

    private static JsonAdresse tilGateAdresse(final Map<String, String> adresse) {
        final JsonGateAdresse gateAdresse = new JsonGateAdresse();
        gateAdresse.setType(Type.GATEADRESSE);
        gateAdresse.setLandkode(finnPropertyEllerNullOmTom(adresse, "landkode"));
        gateAdresse.setKommunenummer(finnPropertyEllerNullOmTom(adresse, "kommunenummer"));
        gateAdresse.setBolignummer(finnPropertyEllerNullOmTom(adresse, "bolignummer"));
        gateAdresse.setGatenavn(finnPropertyEllerNullOmTom(adresse, "gatenavn"));
        gateAdresse.setHusnummer(finnPropertyEllerNullOmTom(adresse, "husnummer"));
        gateAdresse.setHusbokstav(finnPropertyEllerNullOmTom(adresse, "husbokstav"));
        gateAdresse.setPostnummer(finnPropertyEllerNullOmTom(adresse, "postnummer"));
        gateAdresse.setPoststed(finnPropertyEllerNullOmTom(adresse, "poststed"));
        return gateAdresse;
    }

    private static JsonAdresse tilMatrikkelAdresse(final Map<String, String> adresse) {
        final JsonMatrikkelAdresse matrikkelAdresse = new JsonMatrikkelAdresse();
        matrikkelAdresse.setType(Type.MATRIKKELADRESSE);
        matrikkelAdresse.setKommunenummer(finnPropertyEllerNullOmTom(adresse, "kommunenummer"));
        matrikkelAdresse.setGaardsnummer(finnPropertyEllerNullOmTom(adresse, "gaardsnummer"));
        matrikkelAdresse.setBruksnummer(finnPropertyEllerNullOmTom(adresse, "bruksnummer"));
        matrikkelAdresse.setFestenummer(finnPropertyEllerNullOmTom(adresse, "festenummer"));
        matrikkelAdresse.setSeksjonsnummer(finnPropertyEllerNullOmTom(adresse, "seksjonsnummer"));
        matrikkelAdresse.setUndernummer(finnPropertyEllerNullOmTom(adresse, "undernummer"));
        return matrikkelAdresse;
    }

}
