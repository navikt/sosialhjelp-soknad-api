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
        handlebars.registerHelper(VariabelHelper.NAVN, VariabelHelper.INSTANS);
    }

    @Test
    public void skalInterpolereVariabel() throws IOException {
        String innhold = handlebars.compileInline("{{#variabel \"envar\" \"variabel.verdi\"}}{{envar}}{{/variabel}}").apply(new Object());
        assertThat(innhold).isEqualTo("variabel.verdi");
    }

    @Test
    public void skalKunInterpolereInneforBlokk() throws IOException {
        String innhold = handlebars.compileInline("{{envar}}{{#variabel \"envar\" \"variabel.verdi\"}}{{/variabel}}{{envar}}").apply(new Object());
        assertThat(innhold).isNotEqualTo("variabel.verdi");
    }

    @Test
    public void skalIkkeDeleVariable() throws IOException {
        String innhold = handlebars.compileInline("{{#variabel \"envar\" \"variabel.verdi\"}}{{envar}}{{/variabel}}{{#variabel \"envar\" \"variabel.verdi2\"}}{{envar}}{{/variabel}}").apply(new Object());
        assertThat(innhold).contains("variabel.verdi");
        assertThat(innhold).contains("variabel.verdi2");
    }

}