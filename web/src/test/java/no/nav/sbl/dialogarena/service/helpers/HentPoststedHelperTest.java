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
public class HentPoststedHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentPoststedHelper hentTekstHelper;

    @Mock
    Kodeverk kodeverk;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentTekstHelper.getNavn(), hentTekstHelper);
        when(kodeverk.getPoststed("2233")).thenReturn("VESTMARKA");
        when(kodeverk.getPoststed("3580")).thenReturn("GEILO");
    }

    @Test
    public void finnerPoststed() throws IOException {
        String compiled = handlebars.compileInline("{{hentPoststed \"2233\"}}").apply(null);

        assertThat(compiled).isEqualTo("VESTMARKA");
    }


}