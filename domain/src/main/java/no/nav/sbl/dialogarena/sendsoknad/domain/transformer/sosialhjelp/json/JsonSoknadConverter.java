package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;

import java.util.Collections;

public final class JsonSoknadConverter {

    private JsonSoknadConverter() {

    }

    public static JsonSoknad tilJsonSoknad(WebSoknad webSoknad) {
        final JsonSoknad jsonSoknad = new JsonSoknad();
        jsonSoknad.setData(tilData(webSoknad));
        jsonSoknad.setKompatibilitet(Collections.emptyList());

        // TODO: Generer driftsmelding:
        jsonSoknad.setDriftsinformasjon("");

        return jsonSoknad;
    }

    private static JsonData tilData(WebSoknad webSoknad) {
        return new JsonData()
                .withPersonalia(JsonPersonaliaConverter.tilPersonalia(webSoknad))
                .withArbeid(JsonArbeidConverter.tilArbeid(webSoknad))
                .withUtdanning(JsonUtdanningConverter.tilUtdanning(webSoknad))
                .withFamilie(JsonFamilieConverter.tilFamilie(webSoknad))
                .withBegrunnelse(JsonBegrunnelseConverter.tilBegrunnelse(webSoknad));
    }
}
