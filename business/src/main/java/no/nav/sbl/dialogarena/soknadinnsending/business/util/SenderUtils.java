package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;

public class SenderUtils {

    public static String createPrefixedBehandlingsIdInNonProd(String behandlingsId) {
        return environmentNameIfTest() + behandlingsId;
    }

    private static String environmentNameIfTest() {
        if (ServiceUtils.isNonProduction()) {
            final String environment = System.getProperty("environment.name");
            return environment + "-";
        }
        return "";
    }
}
