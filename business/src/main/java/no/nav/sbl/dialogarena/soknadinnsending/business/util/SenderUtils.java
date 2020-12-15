package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;

public class SenderUtils {

    public static String createPrefixedBehandlingsIdInNonProd(String behandlingsId) {
        if (ServiceUtils.isNonProduction()) {
            return System.getProperty("environment.name") + "-" + behandlingsId;
        }
        return behandlingsId;
    }
}
