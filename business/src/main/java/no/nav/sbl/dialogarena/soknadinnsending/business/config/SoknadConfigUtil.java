package no.nav.sbl.dialogarena.soknadinnsending.business.config;

import no.nav.modig.core.exception.ApplicationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoknadConfigUtil {
    private static List<SoknadConfig> SOKNADER = Arrays.asList(
            new AAPConfig(), new DagpengerGjenopptakConfig(), new DagpengerOrdinaerConfig(), new ForeldrepengerConfig()
    );
    public static SoknadConfig getConfig(String skjemanummer) {
        for (SoknadConfig soknadConfig : SOKNADER) {
            if(soknadConfig.getSkjemanummer().contains(skjemanummer)) {
                return soknadConfig;
            }
        }
        throw new ApplicationException("Fant ikke config for skjemanummer: " + skjemanummer);
    }

    public static List<String> getAlleSkjemanummer() {
        List<String> skjemanummer = new ArrayList();
        for (SoknadConfig soknadConfig : SOKNADER) {
            skjemanummer.addAll(soknadConfig.getSkjemanummer());
        }
        return skjemanummer;
    }
}
