package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLBruker;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPerson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;


public class XMLPersonDeserializer extends StdDeserializer<XMLPerson> {
    public XMLPersonDeserializer() {
        this(null);
    }

    public XMLPersonDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public XMLPerson deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

        JsonNode node = parser.getCodec().readTree(parser);
        ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        if (node.has("gjeldendePostadresseType")
                | node.has("elektroniskKommunikasjonskanal")
                | node.has("midlertidigPostadresse")
                | node.has("preferanser")

        ) {
            return mapper.treeToValue(node, XMLBruker.class);
        } else {
            return mapper.treeToValue(node, XMLPerson.class);
        }
    }
}
