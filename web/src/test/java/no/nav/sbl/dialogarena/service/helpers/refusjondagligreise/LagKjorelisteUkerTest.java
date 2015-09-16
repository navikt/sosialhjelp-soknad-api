package no.nav.sbl.dialogarena.service.helpers.refusjondagligreise;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class LagKjorelisteUkerTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        LagKjorelisteUker helper = new LagKjorelisteUker();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalLeggeSammenToTekststrenger() throws IOException {
        String innhold = handlebars.compileInline("myeinnhold").apply(new Object());
        assertThat(innhold).isEqualTo("myeinnhold");
    }

}