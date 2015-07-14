package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class VariabelHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup(){
        handlebars = new Handlebars();
        handlebars.registerHelper(VariabelHelper.NAME, VariabelHelper.INSTANCE);
    }

    @Test
    public void skalInterpolereVariabel() throws IOException {
        String result = handlebars.compileInline("{{#variabel \"envar\" \"variabel.verdi\"}}{{envar}}{{/variabel}}").apply(new Object());
        assertThat(result).isEqualTo("variabel.verdi");
    }

    @Test
    public void skalKunInterpolereInneforBlokk() throws IOException {
        String result = handlebars.compileInline("{{envar}}{{#variabel \"envar\" \"variabel.verdi\"}}{{/variabel}}{{envar}}").apply(new Object());
        assertThat(result).isNotEqualTo("variabel.verdi");
    }

    @Test
    public void skalIkkeDeleVariable() throws IOException {
        String result = handlebars.compileInline("{{#variabel \"envar\" \"variabel.verdi\"}}{{envar}}{{/variabel}}{{#variabel \"envar\" \"variabel.verdi2\"}}{{envar}}{{/variabel}}").apply(new Object());
        assertThat(result).contains("variabel.verdi");
        assertThat(result).contains("variabel.verdi2");
    }

}