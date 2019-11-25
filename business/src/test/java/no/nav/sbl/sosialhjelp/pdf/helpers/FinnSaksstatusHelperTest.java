package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FinnSaksstatusHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        FinnSaksstatusHelper helper = new FinnSaksstatusHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseBeskrivelseVedVedtak() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksstatus \"VEDTATT\" \"Beskrivelse av vedtak\" \"INNVILGET\"}}").apply(new Object());
        assertThat(compiled, is("Innvilget: Beskrivelse av vedtak"));
    }

    @Test
    public void skalLeggePaAvslag() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksstatus \"VEDTATT\" \"Beskrivelse av vedtak\" \"AVSLAG\"}}").apply(new Object());
        assertThat(compiled, is("Avslag: Beskrivelse av vedtak"));
    }

    @Test
    public void skalLeggePaAvvist() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksstatus \"VEDTATT\" \"Beskrivelse av vedtak\" \"AVVIST\"}}").apply(new Object());
        assertThat(compiled, is("Avvist: Beskrivelse av vedtak"));
    }

    @Test
    public void skalViseUnderBehandlingVedIkkeVedtak() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksstatus \"UNDER_BEHANDLING\" \"Beskrivelse av vedtak\"}}").apply(new Object());
        assertThat(compiled, is("Under behandling"));
    }

}