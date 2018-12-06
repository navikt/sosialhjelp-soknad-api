package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresseNorsk;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;


public class XMLPostboksadresseDeserializer extends StdDeserializer<XMLPostboksadresse> {
    public XMLPostboksadresseDeserializer() {
        this(null);
    }

    public XMLPostboksadresseDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public XMLPostboksadresse deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

        final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        return mapper.treeToValue(node, XMLPostboksadresseNorsk.class);
    }
}

