package no.nav.sosialhjelp.soknad.oppslag;

import no.nav.sosialhjelp.soknad.oppslag.dto.KontonummerDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KontonummerServiceTest {

    @Mock
    private OppslagConsumer oppslagConsumer;

    @InjectMocks
    private KontonummerService kontonummerService;

    @Test
    public void clientReturnererKontonummer() {
        when(oppslagConsumer.getKontonummer(anyString())).thenReturn(new KontonummerDto("1337"));

        var kontonummer = kontonummerService.getKontonummer("ident");

        assertThat(kontonummer).isEqualTo("1337");
    }

    @Test
    public void clientReturnererKontonummerNull() {
        when(oppslagConsumer.getKontonummer(anyString())).thenReturn(new KontonummerDto(null));

        var kontonummer = kontonummerService.getKontonummer("ident");

        assertThat(kontonummer).isNull();
    }

    @Test
    public void clientReturnererNull() {
        when(oppslagConsumer.getKontonummer(anyString())).thenReturn(null);

        var kontonummer = kontonummerService.getKontonummer("ident");

        assertThat(kontonummer).isNull();
    }
}