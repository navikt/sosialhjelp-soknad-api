package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HvisLikHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisLikHelper helper = new HvisLikHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void viserInnholdVedToLikeStrenger() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisLik \"verdi\" \"verdi\" }}like verdier{{/hvisLik}}").apply(new Object());
        assertThat(compiled).isEqualTo("like verdier");
    }


    @Test
    public void viserIkkeInnholdVedToUlikeStrenger() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisLik \"noe\" \"noe annet\" }}like verdier{{else}}ulike verdier{{/hvisLik}}").apply(new Object());
        assertThat(compiled).isEqualTo("ulike verdier");
    }

}