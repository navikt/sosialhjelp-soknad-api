package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;

import java.util.HashMap;
import java.util.Map;

public class FiksMetadataTransformer implements EkstraMetadataTransformer {

    public static final String FIKS_ORGNR_KEY = "fiksorgnr";
    public static final String FIKS_ENHET_KEY = "fiksenhet";

    @Override
    public Map<String, String> apply(WebSoknad webSoknad) {
        HashMap<String, String> map = new HashMap<>();

        map.put(FIKS_ORGNR_KEY, "123456789");
        map.put(FIKS_ENHET_KEY, "NAV Horten");

        return map;
    }
}
