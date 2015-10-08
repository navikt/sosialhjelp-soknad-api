package no.nav.sbl.dialogarena.rest.ressurser;

import no.nav.sbl.dialogarena.soknadinnsending.business.FunksjonalitetBryter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InternalRessursTest {

    @InjectMocks
    InternalRessurs ressurs;

    @Test
    public void skalSkruPaFunksjonalitetBryterNarStatusErSattTilTrue() throws InterruptedException {
        List<String> brytere = new ArrayList<>();
        List<String> status = new ArrayList<>();

        brytere.add("0" + FunksjonalitetBryter.GammelVedleggsLogikk.name());
        status.add("0" + "true");

        ressurs.endreFunksjonalitetBryter(brytere, status);

        assertThat(FunksjonalitetBryter.GammelVedleggsLogikk.erAktiv()).isTrue();
    }

    @Test
    public void skalSkruAvFunksjonalitetBryterNarStatusIkkeErSatt() throws InterruptedException {
        List<String> brytere = new ArrayList<>();
        List<String> status = new ArrayList<>();

        brytere.add("0" + FunksjonalitetBryter.GammelVedleggsLogikk.name());

        ressurs.endreFunksjonalitetBryter(brytere, status);

        assertThat(FunksjonalitetBryter.GammelVedleggsLogikk.erAktiv()).isFalse();
    }
}
