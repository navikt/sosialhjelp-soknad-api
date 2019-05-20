package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;


import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;


public class XMLMidlertidigPostadresseDeserializer extends StdDeserializer<XMLMidlertidigPostadresse> {
    public XMLMidlertidigPostadresseDeserializer() {
        this(null);
    }

    public XMLMidlertidigPostadresseDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public XMLMidlertidigPostadresse deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

        final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        if (node.has("ustrukturertAdresse")) {
            return mapper.treeToValue(node, XMLMidlertidigPostadresseUtland.class);
        } else {
            return mapper.treeToValue(node, XMLMidlertidigPostadresseNorge.class);
        }
    }
}
