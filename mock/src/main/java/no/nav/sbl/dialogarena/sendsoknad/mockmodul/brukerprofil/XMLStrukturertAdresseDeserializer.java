package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;

import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLPostboksadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStedsadresse;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStrukturertAdresse;

import java.io.IOException;

public class XMLStrukturertAdresseDeserializer extends StdDeserializer<XMLStrukturertAdresse> {
    public XMLStrukturertAdresseDeserializer() {
        this(null);
    }

    public XMLStrukturertAdresseDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public XMLStrukturertAdresse deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

        JsonNode node = parser.getCodec().readTree(parser);
        ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        if (node.has("postboksnummer")) {
            return mapper.treeToValue(node, XMLPostboksadresse.class);
        } else {
            return mapper.treeToValue(node, XMLStedsadresse.class);
        }
    }
}
