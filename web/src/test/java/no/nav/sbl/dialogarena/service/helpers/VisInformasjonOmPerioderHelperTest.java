package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.ForeldrepengerInformasjon;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
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
    public void viserIkkeInnholdNarSoknadHarOpprettetDatoFørFeilretting() throws IOException {
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{/visInformasjonOmPerioder}}").apply(
                opprettSoknad(ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.get(0), "2018-04-17 00:00"));
        assertThat(compiled).isEqualTo("JA");
    }

    @Test
    public void viserIkkeInnholdNarSoknadHarOpprettetTidspunktFørFeilretting() throws IOException {
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{/visInformasjonOmPerioder}}").apply(
                opprettSoknad(ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.get(0), "2018-04-17 15:59"));
        assertThat(compiled).isEqualTo("JA");
    }

    @Test
    public void viserIkkeInnholdNarSoknadHarOpprettetTidspunktRettEtterFeilretting() throws IOException {
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{/visInformasjonOmPerioder}}").apply(
                opprettSoknad(ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.get(0), "2018-04-17 16:01"));
        assertThat(compiled).isEmpty();
    }


    @Test
    public void viserIkkeInnholdNarSoknadHarSenOpprettetDato() throws IOException {
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{/visInformasjonOmPerioder}}").apply(
                opprettSoknad(ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.get(0), "2018-04-18 00:00"));
        assertThat(compiled).isEmpty();
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

    @Test
    public void viserAnnetInnholdNarBarnetErFodtForForsteMars() throws IOException {
        WebSoknad soknad = opprettSoknad(ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.get(0));
        soknad.medFaktum(new Faktum().medKey("barnet.dato").medValue("2018-02-28"));
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{else}}NEI{{/visInformasjonOmPerioder}}").apply(
                soknad
        );
        assertThat(compiled).isEqualTo("NEI");
    }

    @Test
    public void viserAnnetInnholdNarBarnetErFodtEtterMars() throws IOException {
        WebSoknad soknad = opprettSoknad(ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.get(0));
        soknad.medFaktum(new Faktum().medKey("barnet.dato").medValue("2018-03-01"));
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{else}}NEI{{/visInformasjonOmPerioder}}").apply(
                soknad
        );
        assertThat(compiled).isEqualTo("JA");
    }

    @Test
    public void viserAnnetInnholdNarBarnetErFodtForsteJuli() throws IOException {
        WebSoknad soknad = opprettSoknad(ForeldrepengerInformasjon.FORSTEGANGSSOKNADER.get(0));
        soknad.medFaktum(new Faktum().medKey("barnet.dato").medValue("2018-07-01"));
        String compiled = handlebars.compileInline("{{#visInformasjonOmPerioder}}JA{{else}}NEI{{/visInformasjonOmPerioder}}").apply(
                soknad
        );
        assertThat(compiled).isEqualTo("NEI");
    }


    private WebSoknad opprettSoknad(String skjemaNummer, String opprettetDato) {
        WebSoknad soknad = new WebSoknad();
        soknad.setSkjemaNummer(skjemaNummer);
        soknad.medOppretteDato(DateTime.parse(opprettetDato, DateTimeFormat.forPattern("y-M-d H:m")));
        return soknad;
    }

    private WebSoknad opprettSoknad(String skjemaNummer) {
        WebSoknad soknad = opprettSoknad(skjemaNummer, "2018-04-01 00:00");
        return soknad;
    }
}