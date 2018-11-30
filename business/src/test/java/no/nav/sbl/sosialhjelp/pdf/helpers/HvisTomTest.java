package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HvisTomTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisTomHelper helper = new HvisTomHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void viserIkkeInnholdDersomVerdiErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisTom \"verdi\" }}{{/hvisTom}}").apply(new Object());
        assertThat(compiled).isEqualTo("");
    }


    @Test
    public void viserInnholdDersomVerdiIkkeErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisTom \"\"}}Ikke tom verdi{{else}}{{/hvisTom}}").apply(new Object());
        assertThat(compiled).isEqualTo("Ikke tom verdi");
    }

}
