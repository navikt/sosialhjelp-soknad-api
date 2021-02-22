package no.nav.sosialhjelp.soknad.mock.utbetaling;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSAktoer;
import no.nav.tjeneste.virksomhet.utbetaling.v1.informasjon.WSPerson;

import java.io.IOException;

public class WSAktoerDeserializer extends StdDeserializer<WSAktoer> {
    public WSAktoerDeserializer() {
        this(null);
    }

    public WSAktoerDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public WSAktoer deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        return mapper.treeToValue(node, WSPerson.class);
    }
}
