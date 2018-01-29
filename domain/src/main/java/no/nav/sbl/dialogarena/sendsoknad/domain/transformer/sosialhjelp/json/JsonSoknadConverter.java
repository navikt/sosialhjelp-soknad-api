package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json;

import java.util.Collections;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;

public final class JsonSoknadConverter {

    private JsonSoknadConverter() {
        
    }
    
    
    public static JsonSoknad toJsonSoknad(WebSoknad webSoknad) {
        final JsonSoknad jsonSoknad = new JsonSoknad();
        jsonSoknad.setData(toData(webSoknad));
        jsonSoknad.setKompatibilitet(Collections.emptyList());
        
        // TODO: Generer driftsmelding:
        jsonSoknad.setDriftsinformasjon("");
        
        return jsonSoknad;
    }

    
    private static JsonData toData(WebSoknad webSoknad) {
        final JsonData data = new JsonData();
        data.setPersonalia(JsonPersonaliaConverter.toPersonalia(webSoknad));
        return data;
    }
}
