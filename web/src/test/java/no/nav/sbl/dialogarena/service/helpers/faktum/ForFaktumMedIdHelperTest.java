package no.nav.sbl.dialogarena.service.helpers.faktum;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ForFaktumMedIdHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        ForFaktumMedIdHelper helper = new ForFaktumMedIdHelper();
        handlebars.registerHelper(helper.getNavn(), helper);

    }

    @Test
    public void skalFinneFaktumMedIdOgSettePaaContext() throws IOException {
        Context websoknadMedFaktum = Context.newContext(new WebSoknad().medFaktum(new Faktum().medFaktumId(1L).medValue("testverdi")));
        String compiled = handlebars.compileInline("{{#forFaktumMedId \"1\" }}faktum finnes med verdi {{value}}{{else}}finnes ikke{{/forFaktumMedId}}").apply(websoknadMedFaktum);
        assertThat(compiled).isEqualTo("faktum finnes med verdi testverdi");
    }

    @Test
    public void skalFinneFaktumMedPropertyOgSettePaaContext() throws IOException {
        Context websoknadMedFaktum = Context.newContext(new WebSoknad().medFaktum(new Faktum().medFaktumId(2L).medProperty("propertynavn", "propertverdi")));
        String compiled = handlebars.compileInline("{{#forFaktumMedId \"2\" }}faktum finnes med verdi {{properties.propertynavn}}{{else}}finnes ikke{{/forFaktumMedId}}").apply(websoknadMedFaktum);
        assertThat(compiled).isEqualTo("faktum finnes med verdi propertverdi");
    }

    @Test
    public void skalBrukeInverseOmFaktumIkkeFinnes() throws IOException {
        Context tomWebsoknad = Context.newContext(new WebSoknad());
        String compiled = handlebars.compileInline("{{#forFaktumMedId \"3\" }}faktum finnes med verdi {{value}}{{else}}finnes ikke{{/forFaktumMedId}}").apply(tomWebsoknad);
        assertThat(compiled).isEqualTo("finnes ikke");
    }
}