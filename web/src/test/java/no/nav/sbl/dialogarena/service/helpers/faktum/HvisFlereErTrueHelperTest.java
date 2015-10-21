package no.nav.sbl.dialogarena.service.helpers.faktum;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HvisFlereErTrueHelperTest {


    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisFlereErTrueHelper helper = new HvisFlereErTrueHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void viserOmFlereErTrue() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("min.key.1").medValue("true"))
                .medFaktum(new Faktum().medKey("min.key.2").medValue("false"))
                .medFaktum(new Faktum().medKey("heltannenkey").medValue("true"))
                .medFaktum(new Faktum().medKey("min.key.3").medValue("true"));

        String compiled = handlebars.compileInline("{{#hvisFlereErTrue \"min.key\" \"1\" }}flere er true{{else}}Dette skal ikke vises.{{/hvisFlereErTrue}}").apply(soknad);
        assertThat(compiled).isEqualTo("flere er true");
    }

    @Test
    public void viserOmFlereIkkeErTrue() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("min.key.1").medValue("true"))
                .medFaktum(new Faktum().medKey("min.key.2").medValue("false"))
                .medFaktum(new Faktum().medKey("heltannenkey").medValue("true"))
                .medFaktum(new Faktum().medKey("min.key.3").medValue("true"));

        String compiled = handlebars.compileInline("{{#hvisFlereErTrue \"min.key\" \"2\" }}skal ikke vises{{else}}ikke nok true{{/hvisFlereErTrue}}").apply(soknad);
        assertThat(compiled).isEqualTo("ikke nok true");
    }
}