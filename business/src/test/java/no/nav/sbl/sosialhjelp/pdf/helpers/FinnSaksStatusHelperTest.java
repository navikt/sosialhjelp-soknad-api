package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FinnSaksStatusHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        FinnSaksStatusHelper helper = new FinnSaksStatusHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseBeskrivelseVedVedtak() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksStatus \"VEDTATT\" \"Beskrivelse av vedtak\"}}").apply(new Object());
        assertThat(compiled, is("Beskrivelse av vedtak"));
    }

    @Test
    public void skalViseUnderBehandlingVedIkkeVedtak() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksStatus \"UNDER_BEHANDLING\" \"Beskrivelse av vedtak\"}}").apply(new Object());
        assertThat(compiled, is("Under behandling"));
    }

}