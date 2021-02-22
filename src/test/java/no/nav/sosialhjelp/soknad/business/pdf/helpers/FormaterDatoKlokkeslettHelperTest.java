package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FormaterDatoKlokkeslettHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        FormaterDatoKlokkeslettHelper helper = new FormaterDatoKlokkeslettHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseDatoOgKlokkeslettINorskTidssone() throws IOException {
        Locale.setDefault(Locale.forLanguageTag("nb-NO"));
        String compiled = handlebars.compileInline("{{formaterDatoKlokkeslett \"2018-10-04T13:37:00.134Z\" \"d. MMMM yyyy HH:mm\"}}").apply(new Object());
        assertThat(compiled, is("4. oktober 2018 15:37"));
    }


}