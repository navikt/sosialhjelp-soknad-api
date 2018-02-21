package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;

public class JsonOkonomiConverter {
    public static JsonOkonomi tilOkonomi(WebSoknad webSoknad) {

        final JsonOkonomi okonomi = new JsonOkonomi();

        okonomi.setOpplysninger(tilJsonOpplysninger(webSoknad));
        okonomi.setOversikt(tilJsonOversikt(webSoknad));

        return okonomi;
    }

    private static JsonOkonomioversikt tilJsonOversikt(WebSoknad webSoknad) {
        return new JsonOkonomioversikt()
                /*
                .withInntekt(tilJsonOkonomioversiktInntekt(webSoknad))
                .withUtgift(tilJsonOkonomioversiktUtgift(webSoknad))
                .withFormue(tilJsonOkonomioversiktFormue(webSoknad))
                */
                ;
    }

    private static JsonOkonomiopplysninger tilJsonOpplysninger(WebSoknad webSoknad) {

        return new JsonOkonomiopplysninger()
                /*.withUtbetaling(tilJsonOkonomioversiktUtbetaling(webSoknad))
                .withUtgift(tilJsonOkonomioversiktUtgift(webSoknad))
                .withBekreftelse(tilJsonOkonomioversiktBekreftelse(webSoknad))
                .withBeskrivelseAvAnnet(tilJsonOkonomioversiktBeskrivelseAvAnnet(webSoknad))
                */
                ;

    }
}
