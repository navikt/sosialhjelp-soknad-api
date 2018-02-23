package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.UUID;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonSoknadConverter;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidationException;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;

public class SosialhjelpTilJson implements AlternativRepresentasjonTransformer {

    private static final Logger logger = getLogger(SosialhjelpTilJson.class);

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform(webSoknad);
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.JSON;
    }

    private AlternativRepresentasjon transform(WebSoknad webSoknad) {
        String json;
        try {
            final JsonSoknad jsonSoknad = JsonSoknadConverter.tilJsonSoknad(webSoknad);
            json = new ObjectMapper().writeValueAsString(jsonSoknad);
            JsonSosialhjelpValidator.ensureValidSoknad(json);
        } catch (JsonSosialhjelpValidationException|JsonProcessingException e) {
            logger.error("Kunne ikke generere soknads-JSON for {}", webSoknad.getBrukerBehandlingId(), e);
            throw new AlleredeHandtertException();
        }

        return new AlternativRepresentasjon()
                .medRepresentasjonsType(getRepresentasjonsType())
                .medMimetype("application/json")
                .medFilnavn("soknad.json")
                .medUuid(UUID.randomUUID().toString())
                .medContent(json.getBytes());
    }
}