package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class XMLStedsadresseNorgeDeserializer extends StdDeserializer<XMLStedsadresseNorge> {
    public XMLStedsadresseNorgeDeserializer() {
        this(null);
    }

    public XMLStedsadresseNorgeDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public XMLStedsadresseNorge deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

        JsonNode node = parser.getCodec().readTree(parser);
        ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        if (node.has("gatenummer")) {
            return mapper.treeToValue(node, XMLGateadresse.class);
        } else {
            return mapper.treeToValue(node, XMLMatrikkeladresse.class);
        }
    }
}

