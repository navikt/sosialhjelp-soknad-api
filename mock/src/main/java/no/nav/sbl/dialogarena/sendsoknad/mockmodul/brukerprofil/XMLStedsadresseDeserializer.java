package no.nav.sbl.dialogarena.sendsoknad.mockmodul.brukerprofil;


import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStedsadresse;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.informasjon.XMLStedsadresseNorge;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;


public class XMLStedsadresseDeserializer extends StdDeserializer<XMLStedsadresse> {
    public XMLStedsadresseDeserializer() {
        this(null);
    }

    public XMLStedsadresseDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public XMLStedsadresse deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

        JsonNode node = parser.getCodec().readTree(parser);
        ObjectMapper mapper = (ObjectMapper)parser.getCodec();

        return mapper.treeToValue(node, XMLStedsadresseNorge.class);
    }
}