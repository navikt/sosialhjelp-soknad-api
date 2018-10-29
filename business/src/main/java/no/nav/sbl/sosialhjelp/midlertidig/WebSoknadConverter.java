package no.nav.sbl.sosialhjelp.midlertidig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.InputSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SosialhjelpVedleggTilJson;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.json.JsonSoknadConverter;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.time.*;

import static no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpValidator.ensureValidInternalSoknad;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class WebSoknadConverter {
    private static final Logger logger = getLogger(WebSoknadConverter.class);

    @Inject
    private NavMessageSource messageSource;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

    public SoknadUnderArbeid mapWebSoknadTilSoknadUnderArbeid(WebSoknad webSoknad) {
        if (webSoknad == null) {
            return null;
        }
        return new SoknadUnderArbeid()
                .withVersjon(1L)
                .withBehandlingsId(webSoknad.getBrukerBehandlingId())
                .withTilknyttetBehandlingsId(webSoknad.getBehandlingskjedeId())
                .withEier(webSoknad.getAktoerId())
                .withData(webSoknadTilJson(webSoknad))
                .withInnsendingStatus(webSoknad.getStatus())
                .withOpprettetDato(fraJodaDateTimeTilLocalDateTime(webSoknad.getOpprettetDato()))
                .withSistEndretDato(fraJodaDateTimeTilLocalDateTime(webSoknad.getSistLagret()));
    }

    byte[] webSoknadTilJson(WebSoknad webSoknad) {
        if (!webSoknad.erEttersending()) {
            JsonInternalSoknad jsonInternalSoknad = mapWebSoknadTilJsonSoknadInternal(webSoknad);
            return mapJsonSoknadInternalTilFil(jsonInternalSoknad);
        } else {
            return new byte[1];
        }
    }

    JsonInternalSoknad mapWebSoknadTilJsonSoknadInternal(WebSoknad webSoknad) {
        return new JsonInternalSoknad()
                .withSoknad(JsonSoknadConverter.tilJsonSoknad(new InputSource(webSoknad, messageSource)));
    }

    byte[] mapJsonSoknadInternalTilFil(JsonInternalSoknad jsonInternalSoknad) {
        try {
            final String internalSoknad = writer.writeValueAsString(jsonInternalSoknad);
            ensureValidInternalSoknad(internalSoknad);
            return internalSoknad.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Kunne ikke konvertere søknadsobjekt til tekststreng", e);
        }
        return null;
    }

    LocalDateTime fraJodaDateTimeTilLocalDateTime(DateTime jodaDateTime) {
        if (jodaDateTime == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(jodaDateTime.toInstant().getMillis()), ZoneId.systemDefault());
    }
}
