package no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Aktoer;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.HistoriskArbeidsgiverMedArbeidsgivernummer;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Person;

import java.io.IOException;

public class AktoerDeserializer extends StdDeserializer<Aktoer> {
    public AktoerDeserializer() {
        this(null);
    }

    public AktoerDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public Aktoer deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        final ObjectMapper mapper = (ObjectMapper)parser.getCodec();
        if (node.has("orgnummer")) {
            return mapper.treeToValue(node, Organisasjon.class);
        } else if (node.has("arbeidsgivernummer")) {
            return mapper.treeToValue(node, HistoriskArbeidsgiverMedArbeidsgivernummer.class);
        } else {
            return mapper.treeToValue(node, Person.class);
        }
    }
}
