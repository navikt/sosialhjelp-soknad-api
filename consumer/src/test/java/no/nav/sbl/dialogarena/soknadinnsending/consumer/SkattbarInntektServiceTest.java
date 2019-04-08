package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SkattbarInntektServiceTest {

    @InjectMocks
    private SkattbarInntektService skattbarInntektService;

    @Before
    public void setUp() throws Exception {
        System.setProperty("tillatmock", "true");
    }

    @After
    public void tearDown() throws Exception {
        System.setProperty("tillatmock", "false");
    }

    @Test
    public void hentSkattbarInntekt() {
        List<Utbetaling> utbetalinger = skattbarInntektService.hentSkattbarInntekt("01234567");
        Map<String, List<Utbetaling>> utbetalingPerTittel = utbetalinger.stream().collect(Collectors.groupingBy(o -> o.tittel));
        List<Utbetaling> lonn = utbetalingPerTittel.get("Lønn");

        Utbetaling utbetaling = lonn.get(0);
        assertThat(utbetaling.orgnummer).isEqualTo("911270404");
        assertThat(utbetaling.tittel).isEqualTo("Lønn");
        assertThat(utbetaling.brutto).isEqualTo(40000);
    }
}