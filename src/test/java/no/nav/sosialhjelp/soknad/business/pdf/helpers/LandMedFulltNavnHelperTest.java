package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LandMedFulltNavnHelperTest {

    @Test
    public void skalViseFulltNavnPaaLandGittISO3166Forkortelse() throws IOException {
        final KodeverkService kodeverkService = mock(KodeverkService.class);
        final Handlebars handlebars = createHandlebarsWithHelper(kodeverkService);

        when(kodeverkService.getLand("NOR")).thenReturn("Norge");
        when(kodeverkService.getLand("SWE")).thenReturn("Sverige");

        String compiled = handlebars.compileInline("{{landMedFulltNavn \"NOR\"}}, {{landMedFulltNavn \"SWE\"}}").apply(new Object());
        assertThat(compiled).isEqualTo("Norge, Sverige");
    }

    private Handlebars createHandlebarsWithHelper(KodeverkService kodeverkService) {
        final Handlebars handlebars = new Handlebars();
        final LandMedFulltNavnHelper landMedFulltNavnHelper = new LandMedFulltNavnHelper(kodeverkService);
        handlebars.registerHelper(landMedFulltNavnHelper.getNavn(), landMedFulltNavnHelper);
        return handlebars;
    }
}