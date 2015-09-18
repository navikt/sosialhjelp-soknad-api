package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ForFaktaHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        ForFaktaHelper helper = new ForFaktaHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void finnFaktaOgSettPaContext() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("key").medValue("value"))
                .medFaktum(new Faktum().medKey("key").medValue("value2"));

        Context soknadContext = Context.newContext(soknad);
        String forFaktaHbsTemplate = "{{#forFakta \"key\"}}Faktum med verdi {{value}} {{/forFakta}}";
        String forFaktaCompiled = handlebars.compileInline(forFaktaHbsTemplate).apply(soknadContext);
        assertThat(forFaktaCompiled).isEqualTo("Faktum med verdi value Faktum med verdi value2 ");
    }

    @Test
    public void brukInverseNarFaktaIkkeFinnes() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("key").medValue("value"));

        Context soknadContext = Context.newContext(soknad);
        String forFaktaHbsTemplate = "{{#forFakta \"manglendeKey\"}}Skal ikke slå til{{else}}Faktaliste er tom{{/forFakta}}";
        String forFaktaCompiled = handlebars.compileInline(forFaktaHbsTemplate)
                .apply(soknadContext);

        assertThat(forFaktaCompiled).isEqualTo("Faktaliste er tom");
    }

    @Test
    public void finnFaktaMedPropertiesOgSettPaContext() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("barn").medProperty("navn", "Karsten"))
                .medFaktum(new Faktum().medKey("barn").medProperty("navn", "Petra"));

        Context soknadContext = Context.newContext(soknad);
        String forFaktaHbsTemplate = "{{#forFakta \"barn\"}}Barn {{index}} heter {{properties.navn}} {{else}}Faktaliste er tom{{/forFakta}}";

        String forFaktaCompiled = handlebars.compileInline(forFaktaHbsTemplate)
                .apply(soknadContext);

        assertThat(forFaktaCompiled).isEqualTo("Barn 0 heter Karsten Barn 1 heter Petra ");
    }
}
