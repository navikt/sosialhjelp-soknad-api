package no.nav.sosialhjelp.soknad.consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektConsumer;
import no.nav.sosialhjelp.soknad.consumer.skatt.SkattbarInntektService;
import no.nav.sosialhjelp.soknad.domain.model.skattbarinntekt.SkattbarInntekt;
import no.nav.sosialhjelp.soknad.domain.model.utbetaling.Utbetaling;
import org.apache.cxf.helpers.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SkattbarInntektServiceTest {

    @Mock
    private SkattbarInntektConsumer skattbarInntektConsumer;

    @InjectMocks
    private SkattbarInntektService skattbarInntektService;

    @Test
    public void hentSkattbarInntekt() {
        SkattbarInntekt skattbarInntekt = readResponseFromPath("/mockdata/InntektOgSkatt.json");
        when(skattbarInntektConsumer.hentSkattbarInntekt(anyString())).thenReturn(skattbarInntekt);

        List<Utbetaling> utbetalinger = skattbarInntektService.hentUtbetalinger("01234567");
        Map<String, List<Utbetaling>> utbetalingPerTittel = utbetalinger.stream().collect(Collectors.groupingBy(o -> o.tittel));
        List<Utbetaling> lonn = utbetalingPerTittel.get("Lønnsinntekt");

        Utbetaling utbetaling = lonn.get(0);
        assertThat(utbetaling.brutto).isPositive();
    }

    @Test
    public void hentSkattbarInntektForToMaanederIgnorererDaArbeidsgiver1IForrigeMaaned() {
        SkattbarInntekt skattbarInntekt = readResponseFromPath("/mockdata/InntektOgSkattToMaaneder.json");
        when(skattbarInntektConsumer.hentSkattbarInntekt(anyString())).thenReturn(skattbarInntekt);

        List<Utbetaling> utbetalinger = skattbarInntektService.hentUtbetalinger("01234567");
        assertThat(utbetalinger).hasSize(2);
    }

    @Test
    public void hentSkattbarInntektForToMaanederIForrigeMaanedBeggeMaanedeneOgArbeidsgiverneVilVaereMed() {
        SkattbarInntekt skattbarInntekt = readResponseFromPath("/mockdata/InntektOgSkattToMaanederToArbeidsgivere.json");
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