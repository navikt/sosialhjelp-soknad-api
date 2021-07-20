package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class FinnSaksstatusHelperTest {

    private Handlebars handlebars;

    @BeforeEach
    public void setup() {
        handlebars = new Handlebars();
        FinnSaksstatusHelper helper = new FinnSaksstatusHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    void skalViseBeskrivelseVedVedtak() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksstatus \"VEDTATT\" \"Beskrivelse av vedtak\" \"INNVILGET\"}}").apply(new Object());
        assertThat(compiled).isEqualTo("Innvilget: Beskrivelse av vedtak");
    }

    @Test
    void skalLeggePaAvslag() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksstatus \"VEDTATT\" \"Beskrivelse av vedtak\" \"AVSLAG\"}}").apply(new Object());
        assertThat(compiled).isEqualTo("Avslag: Beskrivelse av vedtak");
    }

    @Test
    void skalLeggePaAvvist() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksstatus \"VEDTATT\" \"Beskrivelse av vedtak\" \"AVVIST\"}}").apply(new Object());
        assertThat(compiled).isEqualTo("Avvist: Beskrivelse av vedtak");
    }

    @Test
    void skalViseUnderBehandlingVedIkkeVedtak() throws IOException {
        String compiled = handlebars.compileInline("{{finnSaksstatus \"UNDER_BEHANDLING\" \"Beskrivelse av vedtak\"}}").apply(new Object());
        assertThat(compiled).isEqualTo("Under behandling");
    }

}