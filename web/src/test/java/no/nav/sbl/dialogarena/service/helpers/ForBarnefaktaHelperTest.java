package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ForBarnefaktaHelperTest {

    private Handlebars handlebars;
    private Context faktumContext;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        ForBarnefaktaHelper helper = new ForBarnefaktaHelper();
        handlebars.registerHelper(helper.getNavn(), helper);

        Faktum parentFaktum = new Faktum().medKey("parent").medFaktumId(55L);
        WebSoknad soknad = new WebSoknad()
                .medFaktum(parentFaktum)
                .medFaktum(new Faktum().medKey("barn").medValue("value1").medParrentFaktumId(55L))
                .medFaktum(new Faktum().medKey("barn").medValue("value2").medParrentFaktumId(9999999L))
                .medFaktum(new Faktum().medKey("barn").medValue("value3").medParrentFaktumId(55L))
                .medFaktum(new Faktum().medKey("annenkey").medValue("value4").medParrentFaktumId(55L));

        Context soknadContext = Context.newContext(soknad);
        faktumContext = Context.newContext(soknadContext, parentFaktum);
    }

    @Test
    public void itererOverFaktaMedFaktumetPaContextSomParentOgRiktigKey() throws IOException {
        String forFaktaCompiled = handlebars.compileInline("{{#forBarnefakta \"barn\"}}{{value}}, {{/forBarnefakta}}").apply(faktumContext);
        assertThat(forFaktaCompiled).isEqualTo("value1, value3, ");
    }

    @Test
    public void visElseOmIngenFaktaMedKeyFinnesPaaParent() throws IOException {
        String forFaktaCompiled = handlebars.compileInline("{{#forBarnefakta \"UGYLDIGKEY\"}}fantes{{else}}fantes ikke{{/forBarnefakta}}").apply(faktumContext);
        assertThat(forFaktaCompiled).isEqualTo("fantes ikke");
    }
}
