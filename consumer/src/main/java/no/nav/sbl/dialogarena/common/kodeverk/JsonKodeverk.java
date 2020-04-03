package no.nav.sbl.dialogarena.common.kodeverk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonKodeverk extends BaseKodeverk {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonKodeverk.class);

    public JsonKodeverk(InputStream json) {
        try {
            this.traverseSkjemaerAndInsertInMap(this.getSkjemaer(json));
        } catch (IOException var3) {
            LOGGER.error("Klarte ikke å parse kodeverk-info", var3);
            throw new SosialhjelpSoknadApiException("Klarte ikke å parse kodeverk-info", var3);
        }
    }

    private ArrayNode getSkjemaer(InputStream json) throws IOException {
        return (ArrayNode)(new ObjectMapper()).readTree(json).get("Skjemaer");
    }

    private void traverseSkjemaerAndInsertInMap(ArrayNode kodeverkArray) {
        Iterator var2 = kodeverkArray.iterator();

        while(var2.hasNext()) {
            JsonNode node = (JsonNode)var2.next();
            Map<Nokkel, String> skjema = new HashMap();
            Map<Nokkel, String> vedlegg = new HashMap();
            this.byggOppSkjema(node, skjema);
            this.dbSkjema.put(this.getFieldValue(node, "Skjemanummer"), new KodeverkElement(skjema));
            if (!"".equals(this.getOptionalFieldValue(node, "Vedleggsid"))) {
                this.byggOppSkjema(node, vedlegg);
                this.dbVedlegg.put(this.getFieldValue(node, "Vedleggsid"), new KodeverkElement(vedlegg));
            }
        }

    }

    private void byggOppSkjema(JsonNode node, Map<Nokkel, String> map) {
        map.put(Nokkel.SKJEMANUMMER, this.getOptionalFieldValue(node, "Skjemanummer"));
        map.put(Nokkel.GOSYS_ID, this.getOptionalFieldValue(node, "Gosysid"));
        map.put(Nokkel.VEDLEGGSID, this.getOptionalFieldValue(node, "Vedleggsid"));
        map.put(Nokkel.TEMA, this.getOptionalFieldValue(node, "Tema"));
        map.put(Nokkel.BESKRIVELSE, this.getOptionalFieldValue(node, "Beskrivelse (ID)"));
        map.put(Nokkel.TITTEL, this.getFieldValue(node, "Tittel"));
        map.put(Nokkel.TITTEL_EN, this.getFieldValue(node, "Tittel_en"));
        map.put(Nokkel.URL, this.getOptionalFieldValue(node, "Lenke"));
        map.put(Nokkel.URLENGLISH, this.getOptionalFieldValue(node, "Lenke engelsk skjema"));
        map.put(Nokkel.URLNEWNORWEGIAN, this.getOptionalFieldValue(node, "Lenke nynorsk skjema"));
        map.put(Nokkel.URLPOLISH, this.getOptionalFieldValue(node, "Lenke polsk skjema"));
        map.put(Nokkel.URLFRENCH, this.getOptionalFieldValue(node, "Lenke fransk skjema"));
        map.put(Nokkel.URLSPANISH, this.getOptionalFieldValue(node, "Lenke spansk skjema"));
        map.put(Nokkel.URLGERMAN, this.getOptionalFieldValue(node, "Lenke tysk skjema"));
        map.put(Nokkel.URLSAMISK, this.getOptionalFieldValue(node, "Lenke samisk skjema"));
    }

    private String getFieldValue(JsonNode node, String fieldName) {
        if (node.has(fieldName)) {
            return node.get(fieldName).asText();
        } else {
            LOGGER.error("Mangler obligatorisk felt {} i kodeverket (json)");
            throw new SosialhjelpSoknadApiException("Mangler felt " + fieldName + " i kodeverket json");
        }
    }

    private String getOptionalFieldValue(JsonNode node, String fieldName) {
        return node.has(fieldName) ? node.get(fieldName).asText() : "";
    }
}
