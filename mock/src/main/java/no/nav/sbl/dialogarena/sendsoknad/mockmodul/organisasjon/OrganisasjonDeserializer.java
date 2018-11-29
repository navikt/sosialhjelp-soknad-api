package no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon;

import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.JuridiskEnhet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Orgledd;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet;

import java.io.IOException;

public class OrganisasjonDeserializer extends StdDeserializer<Organisasjon> {
    public OrganisasjonDeserializer() {
        this(null);
    }

    public OrganisasjonDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public Organisasjon deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

        final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        if (node.has("driverVirksomhet")) {
            return mapper.treeToValue(node, JuridiskEnhet.class);
        } else if (node.has("inngaarIJuridiskEnhet")){
            return mapper.treeToValue(node, Virksomhet.class);

        } else if (node.has("underOrgledd")) {
            return mapper.treeToValue(node, Orgledd.class);

        } else {
            return mapper.treeToValue(node, Organisasjon.class);
        }
    }
}
