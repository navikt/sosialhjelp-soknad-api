package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;
import static org.assertj.core.api.Assertions.assertThat;

public class FormaterDatoHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        FormaterDatoHelper helper = new FormaterDatoHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseDagerFormat() throws IOException {
        String innhold = handlebars.compileInline("{{formaterDato \"2015-09-16\" \"EEEE\"}}, {{formaterDato \"2015-09-20\" \"EEEE\"}}").apply(new Object());
        assertThat(innhold).isEqualTo("onsdag, søndag");
    }

    @Test
    public void skalViseDagerOgDatoFormat() throws IOException {
        String innhold = handlebars.compileInline("{{formaterDato \"2015-07-21\" \"EEEE d. MMMM YYYY\"}}").apply(new Object());
        assertThat(innhold).isEqualTo("tirsdag 21. juli 2015");
    }
    
    @Test
    public void skalViseDatoOgKlokkeslettINorskTidssone() throws IOException {
        String innhold = handlebars.compileInline("{{formaterDato \"2018-10-04T13:37:00.134Z\" \"d. MMMM yyyy HH:mm\"}}").apply(new Object());
        assertThat(innhold).isEqualTo("4. oktober 2018 15:37");
    }


}