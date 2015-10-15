package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HentLandHelperTest {

    private Handlebars handlebars;

    @Mock
    Kodeverk kodeverk;

    @InjectMocks
    HentLandHelper hentLandHelper;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentLandHelper.getNavn(), hentLandHelper);
        when(kodeverk.getLand("NOR")).thenReturn("Norge");
        when(kodeverk.getLand("FRA")).thenReturn("Frankrike");
    }

    @Test
    public void returnererRiktigLand() throws IOException {
        String compiled = handlebars.compileInline("{{hentLand \"NOR\"}}").apply(null);

        assertThat(compiled).isEqualTo("Norge");
    }
}