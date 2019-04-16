package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.InputSource;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;

public class JsonOkonomiConverter {
    public static JsonOkonomi tilOkonomi(InputSource inputSource) {

        JsonOkonomi okonomi = new JsonOkonomi();

        okonomi.setOpplysninger(JsonOkonomiOpplysningerConverter.tilJsonOpplysninger(inputSource));
        okonomi.setOversikt(JsonOkonomiOversiktConverter.tilJsonOversikt(inputSource));

        return okonomi;
    }

}

