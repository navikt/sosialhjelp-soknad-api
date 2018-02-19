package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public final class JsonBegrunnelseConverter {

    private static final Logger logger = getLogger(JsonBegrunnelseConverter.class);

    private JsonBegrunnelseConverter() {

    }

    public static JsonBegrunnelse tilBegrunnelse(WebSoknad webSoknad) {
        String hvorfor = webSoknad.getValueForFaktum("begrunnelse.hvorfor");
        String hva = webSoknad.getValueForFaktum("begrunnelse.hva");
        return new JsonBegrunnelse()
                .withKilde(JsonKildeBruker.BRUKER)
                .withHvorforSoke(hvorfor)
                .withHvaSokesOm(hva);
    }
}
