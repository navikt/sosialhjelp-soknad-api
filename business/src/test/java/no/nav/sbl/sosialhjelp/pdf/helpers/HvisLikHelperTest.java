package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HvisLikHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() {
        handlebars = new Handlebars();
        HvisLikHelper helper = new HvisLikHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void viserInnholdVedToLikeStrenger() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisLik \"verdi\" \"verdi\" }}like verdier{{/hvisLik}}").apply(new Object());
        assertThat(compiled, is("like verdier"));
    }


    @Test
    public void viserIkkeInnholdVedToUlikeStrenger() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisLik \"noe\" \"noe annet\" }}like verdier{{else}}ulike verdier{{/hvisLik}}").apply(new Object());
        assertThat(compiled, is("ulike verdier"));
    }

}