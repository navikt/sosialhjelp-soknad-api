package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class DokumentTypeId {

    private static Map<String, String> map = fillMap();

    private static HashMap<String, String> fillMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("H1", "***REMOVED***");
        map.put("T7", "***REMOVED***");
        map.put("L9", "***REMOVED***");
        map.put("L4", "***REMOVED***");
        map.put("N9", "***REMOVED***");
        map.put("O5", "***REMOVED***");
        map.put("P5", "***REMOVED***");
        map.put("T8", "***REMOVED***");
        map.put("Y4", "***REMOVED***");
        map.put("N6", "***REMOVED***");
        map.put("K3", "***REMOVED***");
        map.put("K4", "***REMOVED***");
        map.put("K1", "***REMOVED***");
        map.put("M6", "***REMOVED***");
        map.put("O9", "***REMOVED***");
        map.put("P3", "***REMOVED***");
        map.put("R4", "***REMOVED***");
        map.put("T1", "***REMOVED***");
        map.put("Z6", "***REMOVED***");
        return map;
    }

    public static String get(String skjemanummer) {
        if (!map.containsKey(skjemanummer)) {
            throw new IllegalArgumentException(format("Ingen dokumentypeid for [%s] finnes", skjemanummer));
        }
        return map.get(skjemanummer);
    }
}
