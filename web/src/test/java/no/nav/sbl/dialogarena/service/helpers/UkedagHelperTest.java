package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class UkedagHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        UkedagHelper helper = new UkedagHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseRiktigDager() throws IOException {
        String innhold = handlebars.compileInline("{{ukedag \"2015-09-16\"}}, {{ukedag \"2015-09-20\"}}, {{ukedag \"2015-09-21\"}}").apply(new Object());
        assertThat(innhold).isEqualTo("Onsdag, Søndag, Mandag");
    }


}