package no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.AlleredeHandtertException;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonType;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonSoknadConverter;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidationException;
import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad;
import org.slf4j.Logger;
import org.springframework.context.MessageSource;

import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

public class SosialhjelpTilJson implements AlternativRepresentasjonTransformer {

    private static final Logger logger = getLogger(SosialhjelpTilJson.class);

    private MessageSource messageSource;

    public SosialhjelpTilJson(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public AlternativRepresentasjon apply(WebSoknad webSoknad) {
        return transform(new InputSource(webSoknad, messageSource));
    }

    @Override
    public AlternativRepresentasjonType getRepresentasjonsType() {
        return AlternativRepresentasjonType.JSON;
    }

    private AlternativRepresentasjon transform(InputSource inputSource) {
        String json;

        try {
            final JsonSoknad jsonSoknad = JsonSoknadConverter.tilJsonSoknad(inputSource);
            final ObjectWriter o = new ObjectMapper().writerWithDefaultPrettyPrinter();
            json = o.writeValueAsString(jsonSoknad);
            JsonSosialhjelpValidator.ensureValidSoknad(json);

        } catch (JsonSosialhjelpValidationException | JsonProcessingException e) {


            logger.error("Kunne ikke generere soknads-JSON for {}", inputSource.getWebSoknad().getBrukerBehandlingId(), e);
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