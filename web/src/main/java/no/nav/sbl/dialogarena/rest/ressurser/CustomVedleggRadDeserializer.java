package no.nav.sbl.dialogarena.rest.ressurser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomVedleggRadDeserializer extends StdDeserializer<VedleggRadFrontend> {

    public CustomVedleggRadDeserializer() {
        this(null);
    }

    public CustomVedleggRadDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public VedleggRadFrontend deserialize(JsonParser jsonparser, DeserializationContext context) throws IOException {
        final ObjectMapper jsonMapper = new ObjectMapper();
        return jsonMapper.readValue(jsonparser, RadAlleFelter.class);
    }
}