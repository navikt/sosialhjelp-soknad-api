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
        final JsonData data = new JsonData();
        data.setPersonalia(JsonPersonaliaConverter.tilPersonalia(webSoknad));
        data.setArbeid(JsonArbeidConverter.tilArbeid(webSoknad));
        data.setUtdanning(JsonUtdanningConverter.tilUtdanning(webSoknad));
        data.setFamilie(JsonFamilieConverter.tilFamilie(webSoknad));
        return data;
    }
}
