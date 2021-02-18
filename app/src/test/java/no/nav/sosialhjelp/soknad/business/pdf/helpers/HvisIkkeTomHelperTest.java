package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HvisIkkeTomHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() {
        handlebars = new Handlebars();
        HvisIkkeTomHelper helper = new HvisIkkeTomHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void viserInnholdDersomVerdiIkkeErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisIkkeTom \"verdi\" }}Ikke tom verdi{{/hvisIkkeTom}}").apply(new Object());
        assertThat(compiled, is("Ikke tom verdi"));
    }


    @Test
    public void viserIkkeInnholdDersomVerdiErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisIkkeTom \"\"}}Ikke tom verdi{{else}}Tom verdi{{/hvisIkkeTom}}").apply(new Object());
        assertThat(compiled, is("Tom verdi"));
    }

}
