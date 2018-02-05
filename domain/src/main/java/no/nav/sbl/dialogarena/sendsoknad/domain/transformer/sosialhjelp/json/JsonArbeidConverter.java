package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonUtils.*;
import static org.slf4j.LoggerFactory.getLogger;

public final class JsonArbeidConverter {

    private static final Logger logger = getLogger(JsonArbeidConverter.class);

    private JsonArbeidConverter() {

    }

    public static JsonArbeid tilArbeid(WebSoknad webSoknad) {
        final JsonArbeid jsonArbeid = new JsonArbeid();

        // TODO: Støtte "situasjon" når AA-registeret er nede.

        jsonArbeid.setForhold(tilJsonArbeidsforhold(webSoknad));
        jsonArbeid.setKommentarTilArbeidsforhold(tilJsonKommentarTilArbeidsforhold(webSoknad));

        return jsonArbeid;
    }

    private static List<JsonArbeidsforhold> tilJsonArbeidsforhold(WebSoknad webSoknad) {
        final List<Faktum> fakta = webSoknad.getFaktaMedKey("arbeidsforhold");

        final List<JsonArbeidsforhold> arbeidsforhold = fakta.stream().map(faktum -> {
            final Map<String, String> forhold = faktum.getProperties();
            return new JsonArbeidsforhold()
                    .withKilde((erAlleSystemProperties(faktum)) ? JsonKilde.SYSTEM : JsonKilde.BRUKER)
                    .withArbeidsgivernavn(finnPropertyEllerNullOmTom(forhold, "arbeidsgivernavn"))
                    .withFom(finnPropertyEllerNullOmTom(forhold, "fom"))
                    .withTom(finnPropertyEllerNullOmTom(forhold, "tom"))
                    .withStillingsprosent(tilInteger(finnPropertyEllerNullOmTom(forhold, "stillingsprosent")))
                    .withStillingstype(tilStillingstype(forhold.get("stillingstype")))
                    
                    /*
                     * Denne må settes hvis man ønsker at bruker skal overstyre systemproperties:
                     * Systemproperty som er overstyrt skal ha "true" som verdi.
                     */
                    .withOverstyrtAvBruker(false)
                    ;
        }).collect(Collectors.toList());

        return arbeidsforhold;
    }

    private static Stillingstype tilStillingstype(String s) {
        if (s == null || s.equals("")) {
            return null;
        }

        switch (s) {
            case "fast":
                return Stillingstype.FAST;
            case "variabel":
                return Stillingstype.VARIABEL;
            case "fastOgVariabel":
                return Stillingstype.FAST_OG_VARIABEL;
        }

        logger.error("Ukjent stillingstype: " + s);
        return null;
    }

    private static Integer tilInteger(String s) {
        if (s == null) {
            return null;
        }
        return Integer.valueOf(s);
    }

    private static JsonKommentarTilArbeidsforhold tilJsonKommentarTilArbeidsforhold(WebSoknad webSoknad) {
        final String kommentar = webSoknad.getValueForFaktum("opplysninger.arbeidsituasjon.kommentarer");
        if (erTom(kommentar)) {
            return null;
        }

        return new JsonKommentarTilArbeidsforhold()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(kommentar);
    }
}
