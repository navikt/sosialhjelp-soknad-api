package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import java.util.HashMap;
import java.util.Map;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.Soknadsmottaker;

public class FiksMetadataTransformer implements EkstraMetadataTransformer {

    public static final String FIKS_ORGNR_KEY = "fiksorgnr";
    public static final String FIKS_ENHET_KEY = "fiksenhet";

    @Override
    public Map<String, String> apply(WebSoknad webSoknad) {
        final HashMap<String, String> map = new HashMap<>();
        final Soknadsmottaker soknadsmottaker = KommuneTilNavEnhetMapper.getSoknadsmottaker(webSoknad);

        if (soknadsmottaker == null) {
            return map;
        }
        
        map.put(FIKS_ORGNR_KEY, soknadsmottaker.getSosialOrgnr());
        map.put(FIKS_ENHET_KEY, soknadsmottaker.getSammensattNavn());

        return map;
    }
}
