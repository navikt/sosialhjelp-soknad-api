package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HvisIkkeTomHelperTest {

    private Handlebars handlebars;

    @BeforeEach
    public void setUp() {
        handlebars = new Handlebars();
        HvisIkkeTomHelper helper = new HvisIkkeTomHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    void viserInnholdDersomVerdiIkkeErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisIkkeTom \"verdi\" }}Ikke tom verdi{{/hvisIkkeTom}}").apply(new Object());
        assertThat(compiled).isEqualTo("Ikke tom verdi");
    }


    @Test
    void viserIkkeInnholdDersomVerdiErTom() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisIkkeTom \"\"}}Ikke tom verdi{{else}}Tom verdi{{/hvisIkkeTom}}").apply(new Object());
        assertThat(compiled).isEqualTo("Tom verdi");
    }

}
