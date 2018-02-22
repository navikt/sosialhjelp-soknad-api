package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibeskrivelserAvAnnet;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonOkonomiConverter {
    public static JsonOkonomi tilOkonomi(WebSoknad webSoknad) {

        final JsonOkonomi okonomi = new JsonOkonomi();

        okonomi.setOpplysninger(tilJsonOpplysninger(webSoknad));
        okonomi.setOversikt(tilJsonOversikt(webSoknad));

        return okonomi;
    }

    private static JsonOkonomioversikt tilJsonOversikt(WebSoknad webSoknad) {
        return new JsonOkonomioversikt()

                .withInntekt(tilJsonOkonomioversiktInntekt(webSoknad))
                .withUtgift(tilJsonOkonomioversiktUtgift(webSoknad))
                .withFormue(tilJsonOkonomioversiktFormue(webSoknad))
                ;
    }


    private static JsonOkonomiopplysninger tilJsonOpplysninger(WebSoknad webSoknad) {

        return new JsonOkonomiopplysninger()
                .withUtbetaling(tilJsonOkonomiopplysningerUtbetaling(webSoknad))
                .withUtgift(tilJsonOkonomiopplysningerUtgift(webSoknad))
                .withBekreftelse(tilJsonOkonomiopplysningerBekreftelse(webSoknad))
                .withBeskrivelseAvAnnet(tilJsonOkonomiopplysningerBeskrivelseAvAnnet(webSoknad))
                ;

    }

    private static JsonOkonomibeskrivelserAvAnnet tilJsonOkonomiopplysningerBeskrivelseAvAnnet(WebSoknad webSoknad) {
        return null;
    }

    private static List<JsonOkonomibekreftelse> tilJsonOkonomiopplysningerBekreftelse(WebSoknad webSoknad) {
        return null;
    }

    private static List<JsonOkonomiOpplysningUtgift> tilJsonOkonomiopplysningerUtgift(WebSoknad webSoknad) {
        return null;
    }

    private static List<JsonOkonomiOpplysningUtbetaling> tilJsonOkonomiopplysningerUtbetaling(WebSoknad webSoknad) {
        return null;
    }


    private static List<JsonOkonomioversiktFormue> tilJsonOkonomioversiktFormue(WebSoknad webSoknad) {
        return null;
    }

    private static List<JsonOkonomioversiktUtgift> tilJsonOkonomioversiktUtgift(WebSoknad webSoknad) {
        return null;
    }

    private static final <E> void addIfNotNull(List<? super E> liste, E... elementer) {
        for (E e : elementer) {
            if (e != null) {
                liste.add(e);
            }
        }
    }

    private static List<JsonOkonomioversiktInntekt> tilJsonOkonomioversiktInntekt(WebSoknad webSoknad) {
        final List<JsonOkonomioversiktInntekt> result = new ArrayList<>();

        final List<Faktum> fakta = webSoknad.getFaktaMedKey("opplysninger.inntekt.bostotte");

        for (Faktum faktum : fakta) {

            List<Faktum> faktumer = new ArrayList<Faktum>();
            faktumer.add(faktum);

            oversiktInntekt("bostotte", "Bostøtte", faktumer, "utbetaling");

        }
        return result;
    }

    private static List<JsonOkonomioversiktInntekt> oversiktInntekt(String type, String tittel, List<Faktum> fakta, String belopNavn) {

        List<JsonOkonomioversiktInntekt> jsonOkonomioversiktInntektList = new ArrayList<JsonOkonomioversiktInntekt>();

        for (Faktum faktum : fakta) {
            jsonOkonomioversiktInntektList.add(oversiktInntekt(type, tittel, faktum, belopNavn, belopNavn));
        }
        return jsonOkonomioversiktInntektList;
    }

    private static JsonOkonomioversiktInntekt oversiktInntekt(String type, String tittel, Faktum faktum, String bruttoNavn, String nettoNavn) {
        if (faktum == null) {
            return null;
        }

        final Map<String, String> properties = faktum.getProperties();
        return new JsonOkonomioversiktInntekt()
                .withKilde(JsonKilde.BRUKER)
                .withType(type)
                .withTittel(tittel)
                .withBrutto(JsonUtils.tilInteger(properties.get(bruttoNavn)))
                .withNetto(JsonUtils.tilInteger(properties.get(nettoNavn)))
                .withOverstyrtAvBruker(false);
    }

}
