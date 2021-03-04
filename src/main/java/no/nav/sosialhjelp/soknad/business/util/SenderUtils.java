package no.nav.sosialhjelp.soknad.business.util;

import no.nav.sosialhjelp.soknad.domain.model.util.ServiceUtils;

public final class SenderUtils {

    private SenderUtils() {
    }

    public static String createPrefixedBehandlingsIdInNonProd(String behandlingsId) {
        if (ServiceUtils.isNonProduction()) {
            return System.getProperty("environment.name") + "-" + behandlingsId;
        }
        return behandlingsId;
    }
}
