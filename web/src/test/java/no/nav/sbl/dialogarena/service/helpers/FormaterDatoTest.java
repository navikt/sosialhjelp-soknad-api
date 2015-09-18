package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class FormaterDatoTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        FormaterDato helper = new FormaterDato();
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


}