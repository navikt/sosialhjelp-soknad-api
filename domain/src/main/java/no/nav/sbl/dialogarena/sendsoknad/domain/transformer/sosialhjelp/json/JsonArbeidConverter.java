package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.empty;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.isSystemProperties;
import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.nullWhenEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;

public final class JsonArbeidConverter {
    
    private static final Logger logger = getLogger(JsonArbeidConverter.class);
    
    private JsonArbeidConverter() {
        
    }
    

    public static JsonArbeid toArbeid(WebSoknad webSoknad) {
        final JsonArbeid jsonArbeid= new JsonArbeid();
        
        // TODO: Støtte "situasjon" når AA-registeret er nede.
        
        jsonArbeid.setForhold(toJsonArbeidsforhold(webSoknad));
        jsonArbeid.setKommentarTilArbeidsforhold(toJsonKommentarTilArbeidsforhold(webSoknad));
        
        return jsonArbeid;
    }


    private static List<JsonArbeidsforhold> toJsonArbeidsforhold(WebSoknad webSoknad) {
        final List<Faktum> fakta = webSoknad.getFaktaMedKey("arbeidsforhold");
        
        final List<JsonArbeidsforhold> arbeidsforhold = fakta.stream().map(faktum -> {
            final Map<String, String> forhold = faktum.getProperties();
            return new JsonArbeidsforhold()
                    .withKilde((isSystemProperties(faktum)) ? JsonKilde.SYSTEM : JsonKilde.BRUKER)
                    .withArbeidsgivernavn(nullWhenEmpty(forhold, "arbeidsgivernavn"))
                    .withFom(nullWhenEmpty(forhold, "fom"))
                    .withTom(nullWhenEmpty(forhold, "tom"))
                    .withStillingsprosent(toInteger(nullWhenEmpty(forhold, "stillingsprosent")))
                    .withStillingstype(toStillingstype(forhold.get("stillingstype")))
                    
                    /*
                     * Denne må settes hvis man ønsker at bruker skal overstyre systemproperties:
                     * Systemproperty som er overstyrt skal ha "true" som verdi.
                     */
                    .withOverstyrtAvBruker(false)
                    ;
        }).collect(Collectors.toList());
        
        return arbeidsforhold;
    }
    
    private static Stillingstype toStillingstype(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        
        switch(s) {
        case "fast": return Stillingstype.FAST;
        case "variabel": return Stillingstype.VARIABEL;
        case "fastOgVariabel": return Stillingstype.FAST_OG_VARIABEL;
        }
        
        logger.error("Ukjent stillingstype: " + s);
        return null;
    }
    
    private static Integer toInteger(String s) {
        if (s == null) {
            return null;
        }
        return Integer.valueOf(s);
    }

    private static JsonKommentarTilArbeidsforhold toJsonKommentarTilArbeidsforhold(WebSoknad webSoknad) {
        final String kommentar = webSoknad.getValueForFaktum("opplysninger.arbeidsituasjon.kommentarer");
        if (empty(kommentar)) {
            return null;
        }
        
        return new JsonKommentarTilArbeidsforhold()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(kommentar);
    }
}
