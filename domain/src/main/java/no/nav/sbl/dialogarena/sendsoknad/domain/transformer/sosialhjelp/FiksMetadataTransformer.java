package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.NavEnhet;

import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.KommuneTilNavEnhetMapper.getNavEnhetFromWebSoknad;

public class FiksMetadataTransformer implements EkstraMetadataTransformer {

    public static final String FIKS_ORGNR_KEY = "fiksorgnr";
    public static final String FIKS_ENHET_KEY = "fiksenhet";

    @Override
    public Map<String, String> apply(WebSoknad webSoknad) {
        HashMap<String, String> map = new HashMap<>();
        NavEnhet navEnhet = getNavEnhetFromWebSoknad(webSoknad);

        map.put(FIKS_ORGNR_KEY, navEnhet.getOrgnummer());
        map.put(FIKS_ENHET_KEY, navEnhet.getNavn());

        return map;
    }
}
