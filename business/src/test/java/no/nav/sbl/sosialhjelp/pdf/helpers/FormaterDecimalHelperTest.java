package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteUtbetaling;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FormaterDecimalHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        FormaterDecimalHelper helper = new FormaterDecimalHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseToDesimalerSomStandard() throws IOException {
        JsonBostotteUtbetaling bostotteUtbetaling = new JsonBostotteUtbetaling().withBelop(123.0);
        String compiled = handlebars.compileInline("{{formaterDecimal belop}}").apply(bostotteUtbetaling);
        assertThat(compiled, is("123,00"));
    }

    @Test
    public void skalkunneViseTreDecimaler() throws IOException {
        JsonBostotteUtbetaling bostotteUtbetaling = new JsonBostotteUtbetaling().withBelop(123.0);
        String compiled = handlebars.compileInline("{{formaterDecimal belop 3}}").apply(bostotteUtbetaling);
        assertThat(compiled, is("123,000"));
    }

}