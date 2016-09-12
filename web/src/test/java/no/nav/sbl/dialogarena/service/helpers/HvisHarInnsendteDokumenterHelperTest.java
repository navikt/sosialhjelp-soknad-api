package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HvisHarInnsendteDokumenterHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisHarInnsendteDokumenterHelper helper = new HvisHarInnsendteDokumenterHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseAtSoknadenHarInnsendteDokumenter() throws Exception {
        WebSoknad webSoknad = SoknadTestHelper.soknadMedInnsendtVedlegg();

        String compiled = handlebars.compileInline("{{#hvisHarInnsendteDokumenter}}har noen innsendte dokumenter{{/hvisHarInnsendteDokumenter}}").apply(Context.newContext(webSoknad));
        assertThat(compiled).isEqualTo("har noen innsendte dokumenter");
    }

    @Test
    public void skalViseAtSoknadenHarIkkeInnsendteDokumenter() throws Exception {
        WebSoknad webSoknad = SoknadTestHelper.soknadMedIkkeInnsendtVedlegg();

        String compiled = handlebars.compileInline("{{#hvisHarInnsendteDokumenter}}har noen innsendte dokumenter{{else}}har ingen innsendte dokumenter{{/hvisHarInnsendteDokumenter}}").apply(Context.newContext(webSoknad));
        assertThat(compiled).isEqualTo("har ingen innsendte dokumenter");
    }


    @Test
    public void viserOmInnsendteDokumenterSelvOmWebSoknadIParentContext() throws Exception {
        WebSoknad webSoknad = SoknadTestHelper.soknadMedInnsendtVedlegg();

        Context parentContext = Context.newContext(webSoknad);
        Context childContext = Context.newContext(parentContext, "random");

        String compiled = handlebars.compileInline("{{#hvisHarInnsendteDokumenter}}har noen innsendte dokumenter{{/hvisHarInnsendteDokumenter}}").apply(childContext);
        assertThat(compiled).isEqualTo("har noen innsendte dokumenter");
    }

}
