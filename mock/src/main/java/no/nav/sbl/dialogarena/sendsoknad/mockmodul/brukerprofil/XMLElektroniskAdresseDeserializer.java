package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;


import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;


public class XMLElektroniskAdresseDeserializer extends StdDeserializer<XMLElektroniskAdresse> {
    public XMLElektroniskAdresseDeserializer() {
        this(null);
    }

    public XMLElektroniskAdresseDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public XMLElektroniskAdresse deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

        final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        if (node.has("type")) {
            return mapper.treeToValue(node, XMLTelefonnummer.class);
        } else {
            return mapper.treeToValue(node, XMLEPost.class);
        }
    }
}


