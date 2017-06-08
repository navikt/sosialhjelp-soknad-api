package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.foreldrepenger.engangsstonad;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class DokumentTypeId {

    private static Map<String, String> map = fillMap();

    private static HashMap<String, String> fillMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("H1", "I000006");
        map.put("T7", "I000016");
        map.put("L9", "I000023");
        map.put("L4", "I000033");
        map.put("N9", "I000037");
        map.put("O5", "I000039");
        map.put("P5", "I000042");
        map.put("T8", "I000043");
        map.put("Y4", "I000044");
        map.put("N6", "I000047");
        map.put("K3", "I000051");
        map.put("K4", "I000052");
        map.put("K1", "I000053");
        map.put("M6", "I000054");
        map.put("O9", "I000056");
        map.put("P3", "I000058");
        map.put("R4", "I000059");
        map.put("T1", "I000060");
        map.put("Z6", "I000061");
        return map;
    }

    public static String get(String skjemanummer) {
        if (!map.containsKey(skjemanummer)) {
            throw new IllegalArgumentException(format("Ingen dokumentypeid for [%s] finnes", skjemanummer));
        }
        return map.get(skjemanummer);
    }
}
