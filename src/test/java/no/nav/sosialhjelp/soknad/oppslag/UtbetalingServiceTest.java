package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.oppslag.dto.UtbetalingDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtbetalingServiceTest {

    @Mock
    private OppslagConsumer oppslagConsumer;

    @InjectMocks
    private UtbetalingService utbetalingService;

    @Test
    public void clientReturnererUtbetalinger() {
        when(oppslagConsumer.getUtbetalingerSiste40Dager(anyString())).thenReturn(singletonList(createUtbetaling()));

        var utbetalinger = utbetalingService.getUtbetalingerSiste40Dager("ident");

        assertThat(utbetalinger).hasSize(1);
        var utbetaling = utbetalinger.get(0);
        assertThat(utbetaling.type).isEqualTo("navytelse");
        assertThat(utbetaling.netto).isEqualTo(1000.0);
        assertThat(utbetaling.brutto).isEqualTo(1234.0);
        assertThat(utbetaling.skattetrekk).isEqualTo(200.0);
        assertThat(utbetaling.andreTrekk).isEqualTo(34.0);
        assertThat(utbetaling.bilagsnummer).isEqualTo("bilagsnummer");
        assertThat(utbetaling.utbetalingsdato).isEqualTo(LocalDate.now().minusDays(2));
        assertThat(utbetaling.periodeFom).isEqualTo(LocalDate.now().minusDays(14));
        assertThat(utbetaling.periodeTom).isEqualTo(LocalDate.now().minusDays(2));
        assertThat(utbetaling.komponenter).hasSize(1);
        assertThat(utbetaling.komponenter.get(0).type).isEqualTo("type");
        assertThat(utbetaling.komponenter.get(0).belop).isEqualTo(42.0);
        assertThat(utbetaling.komponenter.get(0).satsType).isEqualTo("sats");
        assertThat(utbetaling.komponenter.get(0).satsBelop).isEqualTo(21.0);
        assertThat(utbetaling.komponenter.get(0).satsAntall).isEqualTo(2.0);
        assertThat(utbetaling.tittel).isEqualTo("tittel");
        assertThat(utbetaling.orgnummer).isEqualTo("orgnr");
    }

    @Test
    public void clientReturnererTomListe() {
        when(oppslagConsumer.getUtbetalingerSiste40Dager(anyString())).thenReturn(emptyList());

        var utbetalinger = utbetalingService.getUtbetalingerSiste40Dager("ident");

        assertThat(utbetalinger).isEmpty();
    }

    @Test
    public void clientReturnererNull() {
        when(oppslagConsumer.getUtbetalingerSiste40Dager(anyString())).thenReturn(null);

        var utbetalinger = utbetalingService.getUtbetalingerSiste40Dager("ident");

        assertThat(utbetalinger).isNull();
    }

    private UtbetalingDto createUtbetaling() {
        return new UtbetalingDto(
                "navytelse",
                1000.0,
                1234.0,
                200.0,
                34.0,
                "bilagsnummer",
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(14),
                LocalDate.now().minusDays(2),
                singletonList(new UtbetalingDto.KomponentDto("type", 42.0, "sats", 21.0, 2.0)),
                "tittel",
                "orgnr");
    }
}
