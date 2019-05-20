package no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.SammensattNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;

import java.io.IOException;

public class SammensattNavnDeserializer extends StdDeserializer<SammensattNavn> {
    public SammensattNavnDeserializer() {
        this(null);
    }

    public SammensattNavnDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public SammensattNavn deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

        final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        return mapper.treeToValue(node, UstrukturertNavn.class);


    }
}
