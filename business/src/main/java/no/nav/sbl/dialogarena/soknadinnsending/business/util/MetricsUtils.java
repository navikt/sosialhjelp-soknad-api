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

    public static int getProsent(int partial, int total) {
        if (total == 0) return 0;
        double prosent = (double) partial/total * 100; // Cast til double for å få desimaler i delingen
        return (int) Math.round(prosent);
    }
}
