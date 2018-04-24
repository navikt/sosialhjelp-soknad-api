package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class VisInformasjonOmPerioderHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        VisInformasjonOmPerioderHelper helper = new VisInformasjonOmPerioderHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void viserInnholdNarSoknadHarRettSkjemanummer() throws IOException {
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{/visInformasjonOmPerioder}}").apply(
                opprettSoknad(ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.get(0)));
        assertThat(compiled).isEqualTo("JA");

        compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{/visInformasjonOmPerioder}}").apply(
                opprettSoknad(ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.get(1)));
        assertThat(compiled).isEqualTo("JA");
    }


    @Test
    public void viserIkkeInnholdNarSoknadHarFeilSkjemanummer() throws IOException {
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{/visInformasjonOmPerioder}}").apply(
                opprettSoknad(ForeldrepengerInformasjon.ENGANGSSTONADER.get(0)));
        assertThat(compiled).isEmpty();
    }

    @Test
    public void viserAnnetInnholdNarSoknadHarFeilSkjemanummer() throws IOException {
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{else}}NEI{{/visInformasjonOmPerioder}}").apply(
                opprettSoknad(ForeldrepengerInformasjon.ENGANGSSTONADER.get(0))
        );
        assertThat(compiled).isEqualTo("NEI");
    }


    private WebSoknad opprettSoknad(String skjemaNummer) {
        WebSoknad soknad = new WebSoknad();
        soknad.setSkjemaNummer(skjemaNummer);
        return soknad;
    }
}