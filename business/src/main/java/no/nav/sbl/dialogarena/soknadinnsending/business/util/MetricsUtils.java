package no.nav.sbl.dialogarena.soknadinnsending.business.util;

public class MetricsUtils {

    public static String navKontorTilInfluxNavn(String mottaker) {
        if (mottaker == null) {
            return "";
        }
        return mottaker
                .replace("NAV", "")
                .replace(",", "");
    }
}
