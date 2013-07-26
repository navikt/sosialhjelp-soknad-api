package no.nav.sbl.dialogarena.soknad.convert.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import no.nav.modig.core.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import static java.util.Map.Entry;

public class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    public static List<JsonInputElement> toList(final JsonObject jsonObject) {
        return new AbstractList<JsonInputElement>() {
            @Override
            public JsonInputElement get(int index) {
                Iterator<Entry<String,JsonElement>> iterator = jsonObject.entrySet().iterator();

                int i = 0;
                while (i < size() && i < index && iterator.hasNext()) {
                    iterator.next();
                    i++;
                }

                if (!iterator.hasNext()) {
                    LOGGER.error("Kunne ikke opprette liste fra JSON");
                    throw new ApplicationException("Kunne ikke bygge opp søknaden");
                }

                Entry<String, JsonElement> entry = iterator.next();
                return new JsonInputElement(entry.getKey(), entry.getValue().getAsJsonObject());
            }

            @Override
            public int size() {
                return jsonObject.entrySet().size();
            }
        };
    }
}
