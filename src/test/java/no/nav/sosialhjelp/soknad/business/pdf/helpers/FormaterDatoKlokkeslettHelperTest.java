package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;


public class FormaterDatoKlokkeslettHelperTest {

    private Handlebars handlebars;

    @BeforeEach
    public void setup() {
        handlebars = new Handlebars();
        FormaterDatoKlokkeslettHelper helper = new FormaterDatoKlokkeslettHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseDatoOgKlokkeslettINorskTidssone() throws IOException {
        Locale.setDefault(Locale.forLanguageTag("nb-NO"));
        String compiled = handlebars.compileInline("{{formaterDatoKlokkeslett \"2018-10-04T13:37:00.134Z\" \"d. MMMM yyyy HH:mm\"}}").apply(new Object());
        assertThat(compiled).isEqualTo("4. oktober 2018 15:37");
    }


}