package no.nav.sosialhjelp.soknad.consumer.skatt;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.finn.unleash.Unleash;
import no.nav.sosialhjelp.soknad.client.skatteetaten.SkatteetatenClient;
import no.nav.sosialhjelp.soknad.client.skatteetaten.dto.SkattbarInntekt;
import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling;
import org.apache.cxf.helpers.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkattbarInntektServiceTest {

    @Mock
    private SkattbarInntektConsumer skattbarInntektConsumer;

    @Mock
    private SkatteetatenClient skatteetatenClient;

    @Mock
    private Unleash unleash;

    @InjectMocks
    private SkattbarInntektService skattbarInntektService;

    @BeforeEach
    void setUp() {
        when(unleash.isEnabled(anyString(), anyBoolean())).thenReturn(false);
    }

    @Test
    void hentSkattbarInntekt() {
        SkattbarInntekt skattbarInntekt = readResponseFromPath("/skatt/InntektOgSkatt.json");
        when(skattbarInntektConsumer.hentSkattbarInntekt(anyString())).thenReturn(skattbarInntekt);

        List<Utbetaling> utbetalinger = skattbarInntektService.hentUtbetalinger("01234567");
        Map<String, List<Utbetaling>> utbetalingPerTittel = utbetalinger.stream().collect(Collectors.groupingBy(o -> o.tittel));
        List<Utbetaling> lonn = utbetalingPerTittel.get("Lønnsinntekt");

        Utbetaling utbetaling = lonn.get(0);
        assertThat(utbetaling.brutto).isPositive();
    }

    @Test
    void hentSkattbarInntektForToMaanederIgnorererDaArbeidsgiver1IForrigeMaaned() {
        SkattbarInntekt skattbarInntekt = readResponseFromPath("/skatt/InntektOgSkattToMaaneder.json");
        when(skattbarInntektConsumer.hentSkattbarInntekt(anyString())).thenReturn(skattbarInntekt);

        List<Utbetaling> utbetalinger = skattbarInntektService.hentUtbetalinger("01234567");
        assertThat(utbetalinger).hasSize(2);
    }

    @Test
    void hentSkattbarInntektForToMaanederIForrigeMaanedBeggeMaanedeneOgArbeidsgiverneVilVaereMed() {
        SkattbarInntekt skattbarInntekt = readResponseFromPath("/skatt/InntektOgSkattToMaanederToArbeidsgivere.json");
        when(skattbarInntektConsumer.hentSkattbarInntekt(anyString())).thenReturn(skattbarInntekt);

        List<Utbetaling> utbetalinger = skattbarInntektService.hentUtbetalinger("01234567");
        assertThat(utbetalinger).hasSize(2);
        assertThat(utbetalinger.stream().collect(Collectors.groupingBy(o -> o.orgnummer)).entrySet()).hasSize(2);
    }


    private SkattbarInntekt readResponseFromPath(String path) {
        try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream(path);
            if (resourceAsStream == null) {
                return null;
            }
            String json = IOUtils.toString(resourceAsStream);
            return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(json, SkattbarInntekt.class);
        } catch (IOException e) {
            return null;
        }
    }
}