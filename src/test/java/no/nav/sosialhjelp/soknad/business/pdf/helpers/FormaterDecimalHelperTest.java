package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class FormaterDecimalHelperTest {

    private Handlebars handlebars;

    @BeforeEach
    public void setup() {
        handlebars = new Handlebars();
        FormaterDecimalHelper helper = new FormaterDecimalHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseToDesimalerSomStandard() throws IOException {
        JsonOkonomiOpplysningUtbetaling bostotteUtbetaling = new JsonOkonomiOpplysningUtbetaling().withNetto(123.0);
        String compiled = handlebars.compileInline("{{formaterDecimal netto}}").apply(bostotteUtbetaling);
        assertThat(compiled).isEqualTo("123,00");
    }

    @Test
    public void skalkunneViseTreDecimaler() throws IOException {
        JsonOkonomiOpplysningUtbetaling bostotteUtbetaling = new JsonOkonomiOpplysningUtbetaling().withNetto(123.0);
        String compiled = handlebars.compileInline("{{formaterDecimal netto 3}}").apply(bostotteUtbetaling);
        assertThat(compiled).isEqualTo("123,000");
    }

}