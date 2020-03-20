package no.nav.sbl.dialogarena.soknadinnsending.consumer.skatt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sendsoknad.domain.skattbarinntekt.SkattbarInntekt;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SkattbarInntektConsumerMock {

    private static final Logger log = LoggerFactory.getLogger(SkattbarInntektConsumerMock.class);

    private static Map<String, SkattbarInntekt> responses = new HashMap<>();

    private static Set<String> mockDataFeiler = new HashSet<>();

    public SkattbarInntektConsumer skattbarInntektConsumerMock() {
        SkattbarInntektConsumer mock = mock(SkattbarInntektConsumer.class);

        when(mock.hentSkattbarInntekt(anyString()))
                .thenAnswer((invocationOnMock) -> getOrDefaultResponse(OidcFeatureToggleUtils.getUserId()));

        return mock;
    }

    public static SkattbarInntekt getOrDefaultResponse(String fnr) {
        SkattbarInntekt response = responses.get(fnr);
        if (response == null) {
            response = defaultUtbetalinger(fnr);
            responses.put(fnr, response);
        }

        return response;
    }

    private static SkattbarInntekt defaultUtbetalinger(String fnr) {
        if (mockDataFeiler.contains(fnr)) {
            return null;
        }
        SkattbarInntekt skattbarInntekt = responses.get(fnr);
        if (skattbarInntekt != null) {
            return skattbarInntekt;
        }
        try {
            InputStream resourceAsStream = SkattbarInntektConsumerMock.class.getResourceAsStream("/mockdata/InntektOgSkatt.json");
            if (resourceAsStream == null) {
                return null;
            }
            String json = IOUtils.toString(resourceAsStream);
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, SkattbarInntekt.class);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public static void setMockData(String fnr, String jsonWSSkattUtbetaling) {
        responses.remove(fnr);
        if (!jsonWSSkattUtbetaling.equalsIgnoreCase("{}")) {
            try {
                SkattbarInntekt skattbarInntekt = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readValue(jsonWSSkattUtbetaling, SkattbarInntekt.class);
                responses.put(fnr, skattbarInntekt);
            } catch (JsonProcessingException e) {
                log.error("", e);
            }
        }
    }

    public static void setMockSkalFeile(String fnr, boolean skalFeile) {
        mockDataFeiler.remove(fnr);
        if (skalFeile) {
            mockDataFeiler.add(fnr);
        }
    }
}
