package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
        String compiled = handlebars.compileInline("{{formaterDato \"2015-09-16\" \"EEEE\"}}, {{formaterDato \"2015-09-20\" \"EEEE\"}}").apply(new Object());
        assertThat(compiled, is("onsdag, søndag"));
    }

    @Test
    public void skalViseDagerOgDatoFormat() throws IOException {
        String compiled = handlebars.compileInline("{{formaterDato \"2015-07-21\" \"EEEE d. MMMM YYYY\"}}").apply(new Object());
        assertThat(compiled, is("tirsdag 21. juli 2015"));
    }

}