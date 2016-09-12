package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HvisHarIkkeInnsendteDokumenterHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisHarIkkeInnsendteDokumenterHelper helper = new HvisHarIkkeInnsendteDokumenterHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseAtSoknadenIkkeHarInnsendteDokumenter() throws Exception {
        WebSoknad webSoknad = SoknadTestHelper.soknadMedIkkeInnsendtVedlegg();

        String compiled = handlebars.compileInline("{{#hvisHarIkkeInnsendteDokumenter}}har ikke-innsendte dokumenter{{/hvisHarIkkeInnsendteDokumenter}}").apply(Context.newContext(webSoknad));
        assertThat(compiled).isEqualTo("har ikke-innsendte dokumenter");
    }

    @Test
    public void skalViseAtSoknadenHarInnsendteDokumenter() throws Exception {
        WebSoknad webSoknad = SoknadTestHelper.soknadMedInnsendtVedlegg();

        String compiled = handlebars.compileInline("{{#hvisHarIkkeInnsendteDokumenter}}har ikke-innsendte dokumenter{{else}}alt er innsendt{{/hvisHarIkkeInnsendteDokumenter}}").apply(Context.newContext(webSoknad));
        assertThat(compiled).isEqualTo("alt er innsendt");
    }


    @Test
    public void viserOmInnsendteDokumenterSelvOmWebSoknadIParentContext() throws Exception {
        WebSoknad webSoknad = SoknadTestHelper.soknadMedIkkeInnsendtVedlegg();

        Context parentContext = Context.newContext(webSoknad);
        Context childContext = Context.newContext(parentContext, "random");

        String compiled = handlebars.compileInline("{{#hvisHarIkkeInnsendteDokumenter}}har ikke-innsendte dokumenter{{/hvisHarIkkeInnsendteDokumenter}}").apply(childContext);
        assertThat(compiled).isEqualTo("har ikke-innsendte dokumenter");
    }


}
