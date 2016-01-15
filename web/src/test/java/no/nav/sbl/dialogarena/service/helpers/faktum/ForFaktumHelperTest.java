package no.nav.sbl.dialogarena.service.helpers.faktum;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ForFaktumHelperTest {
    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        ForFaktumHelper helper = new ForFaktumHelper();
        handlebars.registerHelper(helper.getNavn(), helper);

    }

    @Test
    public void skalFinneFaktumMedValueOgSettePaaContext() throws IOException {
        Context websoknadMedFaktum = Context.newContext(new WebSoknad().medFaktum(new Faktum().medKey("test").medValue("testverdi")));
        String compiled = handlebars.compileInline("{{#forFaktum \"test\" }}faktum finnes med verdi {{value}}{{else}}finnes ikke{{/forFaktum}}").apply(websoknadMedFaktum);
        assertThat(compiled).isEqualTo("faktum finnes med verdi testverdi");
    }

    @Test
    public void skalFinneFaktumMedPropertyOgSettePaaContext() throws IOException {
        Context websoknadMedFaktum = Context.newContext(new WebSoknad().medFaktum(new Faktum().medKey("test").medProperty("propertynavn", "propertverdi")));
        String compiled = handlebars.compileInline("{{#forFaktum \"test\" }}faktum finnes med verdi {{properties.propertynavn}}{{else}}finnes ikke{{/forFaktum}}").apply(websoknadMedFaktum);
        assertThat(compiled).isEqualTo("faktum finnes med verdi propertverdi");
    }

    @Test
    public void skalBrukeInverseOmFaktumIkkeFinnes() throws IOException {
        Context tomWebsoknad = Context.newContext(new WebSoknad());
        String compiled = handlebars.compileInline("{{#forFaktum \"test\" }}faktum finnes med verdi {{value}}{{else}}finnes ikke{{/forFaktum}}").apply(tomWebsoknad);
        assertThat(compiled).isEqualTo("finnes ikke");
    }

    @Test
    public void skalBrukeInverseOmFaktumIkkeHarPropertiesEllerValue() throws IOException {
        Context websoknadMedFaktum = Context.newContext(new WebSoknad().medFaktum(new Faktum().medKey("test")));
        String compiled = handlebars.compileInline("{{#forFaktum \"test\" }}faktum finnes med verdi {{value}}{{else}}finnes ikke{{/forFaktum}}").apply(websoknadMedFaktum);
        assertThat(compiled).isEqualTo("finnes ikke");
    }

}