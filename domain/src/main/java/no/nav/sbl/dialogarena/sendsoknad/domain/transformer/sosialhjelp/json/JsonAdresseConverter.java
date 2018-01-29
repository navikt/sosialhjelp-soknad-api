package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.isFaktumVerdi;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.isSystemProperties;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.nonEmpty;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.nullWhenEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse.Type;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;

public final class JsonAdresseConverter {
    
    private static final Logger logger = getLogger(JsonAdresseConverter.class);
    
    
    private JsonAdresseConverter() {
        
    }
    

    public static JsonAdresse toFolkeregistrertAdresse(WebSoknad webSoknad) {
        final Faktum faktum = webSoknad.getFaktumMedKey("kontakt.system.folkeregistrert.adresse");
        return toSystemAdresse(faktum);
    }
    
    public static JsonAdresse toOppholdsadresse(WebSoknad webSoknad) {
        try {
            if (isFaktumVerdi(webSoknad, "kontakt.adresse.brukerendrettoggle")) {
                final Faktum faktum = webSoknad.getFaktumMedKey("kontakt.adresse.bruker");
                if (faktum == null) {
                    return null;
                }
                
                return toBrukersUstrukturertAdresse(faktum);
            } else {
                final Faktum faktum = webSoknad.getFaktumMedKey("kontakt.system.adresse");
                return toSystemAdresse(faktum);
            }            
        } catch (RuntimeException e) {
            logger.error("Uventet feil: Kan ikke sende med folkeregistrert adresse", e);
            return null;
        }
    }
    
    public static JsonAdresse toPostadresse(WebSoknad webSoknad) {
        /* TODO: Implementer støtte for postadresse. */
        
        final JsonAdresse jsonAdresse = toOppholdsadresse(webSoknad);
        if (jsonAdresse == null) {
            return null;
        }
        if (jsonAdresse.getType() == Type.POSTBOKS) {
            return null;
        }
        return jsonAdresse;
    }


    private static JsonAdresse toBrukersUstrukturertAdresse(final Faktum faktum) {
        /*
         * TODO: Fjerne denne metoden når grensesnittet er endret til at brukeradresse er strukturert.
         */
        final Map<String, String> adresse = faktum.getProperties(); 
        final JsonUstrukturertAdresse ustrukturertAdresse = new JsonUstrukturertAdresse();
        ustrukturertAdresse.setType(Type.USTRUKTURERT);
        ustrukturertAdresse.setKilde(JsonKilde.BRUKER);
        
        final List<String> adresselinjer = new ArrayList<>();
        final String gateadresse = adresse.get("gateadresse");
        if (nonEmpty(gateadresse)) {
            adresselinjer.add(gateadresse);
        }
        final String postnummer = adresse.get("postnummer");
        final String poststed = adresse.get("poststed");
        String postnummerlinje = "";
        if (nonEmpty(postnummer)) {
            postnummerlinje += postnummer;
        }
        if (nonEmpty(poststed)) {
            postnummerlinje += " " + poststed;
        }
        if (!postnummerlinje.trim().equals("")) {
            adresselinjer.add(postnummerlinje);
        }
        
        ustrukturertAdresse.setAdresse(adresselinjer);
        return ustrukturertAdresse;
    }
    
    private static JsonAdresse toSystemAdresse(Faktum faktum) {
        if (faktum == null) {
            return null;
        }

        try {            
            final JsonAdresse adresse = toJsonAdresse(faktum);
            if (adresse != null && adresse.getKilde() != JsonKilde.SYSTEM) {
                throw new IllegalStateException("Systemadresse skal kun innehold systemdata. Har bruker forsøkt å endre adresse med direkte POST-kall? Faktumkey: " + faktum.getKey());
            }
            return adresse;
        } catch (RuntimeException e) {
            logger.error("Uventet feil: Kan ikke sende med folkeregistrert adresse", e);
            return null;
        }
    }
    
    private static JsonAdresse toJsonAdresse(Faktum faktum) {
        final Map<String, String> adresse = faktum.getProperties(); 
        
        final String type = adresse.get("type");
        if (type == null) {
            throw new IllegalStateException("Adresse mangler \"type\": " + faktum.getKey());
        }
        
        JsonAdresse jsonAdresse;
        if (type.equals("gateadresse")) {
            jsonAdresse = toGateAdresse(adresse);
        } else if (type.equals("matrikkeladresse")) { 
            jsonAdresse = toMatrikkelAdresse(adresse);
        } else if (type.equals("ustrukturert")) {
            jsonAdresse = toUstrukturertAdresse(adresse);
        } else if (type.equals("postboks")) {
            throw new IllegalStateException("Adresser av typen \"postboks\" har ikke blitt implementert ennå.");
        } else {
            throw new IllegalStateException("Ukjent adressetype: \"" + type + "\" for faktum: " + faktum.getKey());
        }
        
        jsonAdresse.setKilde(isSystemProperties(faktum) ? JsonKilde.SYSTEM : JsonKilde.BRUKER);
        
        return jsonAdresse;
    }


    private static JsonAdresse toUstrukturertAdresse(Map<String, String> adresse) {
        final String adresseStreng = nullWhenEmpty(adresse, "adresse");
        if (adresseStreng == null) {
            return null;
        }

        final JsonUstrukturertAdresse ustrukturertAdresse = new JsonUstrukturertAdresse();
        ustrukturertAdresse.setType(Type.USTRUKTURERT);
        // TODO: Bruk linjeskift som hentes fra PersonV1/PersonV3 direkte fremfor å kombinere med komma og deretter splitte: 
        ustrukturertAdresse.setAdresse(Arrays.asList(adresseStreng.split(",")).stream().map(s -> s.trim()).collect(Collectors.toList()));

        return ustrukturertAdresse;
    }


    private static JsonAdresse toGateAdresse(final Map<String, String> adresse) {
        final JsonGateAdresse gateAdresse = new JsonGateAdresse();
        gateAdresse.setType(Type.GATEADRESSE);
        gateAdresse.setLandkode(nullWhenEmpty(adresse, "landkode"));
        gateAdresse.setKommunenummer(nullWhenEmpty(adresse, "kommunenummer"));
        gateAdresse.setBolignummer(nullWhenEmpty(adresse, "bolignummer"));
        gateAdresse.setGatenavn(nullWhenEmpty(adresse, "gatenavn"));
        gateAdresse.setHusnummer(nullWhenEmpty(adresse, "husnummer"));
        gateAdresse.setHusbokstav(nullWhenEmpty(adresse, "husbokstav"));
        gateAdresse.setPostnummer(nullWhenEmpty(adresse, "postnummer"));
        gateAdresse.setPoststed(nullWhenEmpty(adresse, "poststed"));
        return gateAdresse;
    }
    
    private static JsonAdresse toMatrikkelAdresse(final Map<String, String> adresse) {
        final JsonMatrikkelAdresse matrikkelAdresse = new JsonMatrikkelAdresse();
        matrikkelAdresse.setType(Type.MATRIKKELADRESSE);
        matrikkelAdresse.setGaardsnummer(nullWhenEmpty(adresse, "gaardsnummer"));
        matrikkelAdresse.setBruksnummer(nullWhenEmpty(adresse, "bruksnummer"));
        matrikkelAdresse.setFestenummer(nullWhenEmpty(adresse, "festenummer"));
        matrikkelAdresse.setSeksjonsnummer(nullWhenEmpty(adresse, "seksjonsnummer"));
        matrikkelAdresse.setUndernummer(nullWhenEmpty(adresse, "undernummer"));
        return matrikkelAdresse;
    }

}
