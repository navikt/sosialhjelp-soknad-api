package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.service.helpers.faktum.ForFaktumHelper;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
 public class HarBarnetInntektHelperTest {

    private Handlebars handlebars;
    private WebSoknad webSoknad;
    private String handlebarInput;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HarBarnetInntektHelper harBarnetInntektHelper = new HarBarnetInntektHelper();
        ForFaktumHelper forFaktumHelper = new ForFaktumHelper();
        handlebars.registerHelper(harBarnetInntektHelper.getNavn(), harBarnetInntektHelper);
        handlebars.registerHelper(forFaktumHelper.getNavn(), forFaktumHelper);

        handlebarInput= "{{#forFaktum \"barn\"}}{{#harBarnetInntekt }}barnet tjener {{value}}{{else}}barnet har ikke inntekt{{/harBarnetInntekt}}{{/forFaktum}}";
    }

    @Test
    public void visInnholdHvisBarnetHarInntekt() throws IOException {
        Faktum harInntekt = new Faktum().medKey("barn.harinntekt").medValue("true").medParrentFaktumId((long) 1);
        Faktum inntekt = new Faktum().medKey("barn.inntekt").medValue("5000").medParrentFaktumId((long) 1);
        Faktum barn = new Faktum().medFaktumId((long) 1).medKey("barn").medValue("Lise");
        webSoknad = new WebSoknad().medFaktum(harInntekt).medFaktum(inntekt).medFaktum(barn);

        String compiled = handlebars.compileInline(handlebarInput).apply(Context.newContext(webSoknad));
        assertThat(compiled).isEqualTo("barnet tjener 5000");
    }

    @Test
    public void brukInverseHvisBarnetIkkeHarInntekt() throws IOException {
        Faktum harInntekt = new Faktum().medKey("barn.harinntekt").medValue("false").medParrentFaktumId((long) 1);
        Faktum barn = new Faktum().medFaktumId((long) 1).medKey("barn").medValue("Lise");
        webSoknad = new WebSoknad().medFaktum(harInntekt).medFaktum(barn);
        String compiled = handlebars.compileInline(handlebarInput).apply(Context.newContext(webSoknad));
        assertThat(compiled).isEqualTo("barnet har ikke inntekt");
    }
}
