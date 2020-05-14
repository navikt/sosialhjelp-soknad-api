package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;

public class JsonOkonomiUtils {
    public static boolean isOkonomiskeOpplysningerBekreftet(JsonOkonomi jsonOkonomi) {
        return jsonOkonomi.getOpplysninger().getBekreftelse() != null && !jsonOkonomi.getOpplysninger().getBekreftelse().isEmpty();
    }
}
