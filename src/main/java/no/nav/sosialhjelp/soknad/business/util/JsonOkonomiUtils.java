package no.nav.sosialhjelp.soknad.business.util;

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;

public final class JsonOkonomiUtils {

    private JsonOkonomiUtils() {
    }

    public static boolean isOkonomiskeOpplysningerBekreftet(JsonOkonomi jsonOkonomi) {
        return jsonOkonomi.getOpplysninger().getBekreftelse() != null && !jsonOkonomi.getOpplysninger().getBekreftelse().isEmpty();
    }
}
