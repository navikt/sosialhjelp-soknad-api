package no.nav.sbl.dialogarena.sendsoknad.domain.transformer;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;

import java.util.Map;
import java.util.function.Function;

public interface EkstraMetadataTransformer extends Function<WebSoknad, Map<String, String>> {
}
