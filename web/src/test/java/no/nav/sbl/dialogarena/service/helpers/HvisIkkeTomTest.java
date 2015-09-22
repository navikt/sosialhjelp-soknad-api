package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HvisIkkeTomTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisIkkeTomHelper helper = new HvisIkkeTomHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void viserInnholdDersomVerdiIkkeErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisIkkeTom \"verdi\" }}Ikke tom verdi{{/hvisIkkeTom}}").apply(new Object());
        assertThat(compiled).isEqualTo("Ikke tom verdi");
    }


    @Test
    public void viserIkkeInnholdDersomVerdiErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisIkkeTom \"\"}}Ikke tom verdi{{else}}Tom verdi{{/hvisIkkeTom}}").apply(new Object());
        assertThat(compiled).isEqualTo("Tom verdi");
    }

}
