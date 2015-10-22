package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ForFaktumHvisSantHelperTest {

    private Handlebars handlebars;
    private WebSoknad soknad;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        ForFaktumHvisSantHelper helper = new ForFaktumHvisSantHelper();
        handlebars.registerHelper(helper.getNavn(), helper);

        soknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("key1").medValue("true").medProperty("prop1", "value1"))
                .medFaktum(new Faktum().medKey("key2").medValue("false").medProperty("prop2", "value2"));
    }

    @Test
    public void skalViseOmFaktumErSant() throws IOException {
        String forFaktaCompiled = handlebars.compileInline("{{#forFaktumHvisSant \"key1\"}}sant{{/forFaktumHvisSant}}").apply(soknad);
        assertThat(forFaktaCompiled).isEqualTo("sant");
    }


    @Test
    public void skalSetteFaktumSomContextNaarSant() throws IOException {
        String forFaktaCompiled = handlebars.compileInline("{{#forFaktumHvisSant \"key1\"}}{{properties.prop1}}{{/forFaktumHvisSant}}").apply(soknad);
        assertThat(forFaktaCompiled).isEqualTo("value1");
    }


    @Test
    public void skalViseOmFaktumIkkeErSant() throws IOException {
        String forFaktaCompiled = handlebars.compileInline("{{#forFaktumHvisSant \"key2\"}}sant{{else}}ikke sant{{/forFaktumHvisSant}}").apply(soknad);
        assertThat(forFaktaCompiled).isEqualTo("ikke sant");
    }


    @Test
    public void skalSetteFaktumSomContextNaarIkkeSant() throws IOException {
        String forFaktaCompiled = handlebars.compileInline("{{#forFaktumHvisSant \"key2\"}}sant{{else}}{{properties.prop2}}{{/forFaktumHvisSant}}").apply(soknad);
        assertThat(forFaktaCompiled).isEqualTo("value2");
    }



    @Test
    public void ikkeSantOmIkkeFaktumFinnes() throws IOException {
        String forFaktaCompiled = handlebars.compileInline("{{#forFaktumHvisSant \"key3\"}}sant{{else}}ikke sant{{properties.prop3}}{{/forFaktumHvisSant}}").apply(soknad);
        assertThat(forFaktaCompiled).isEqualTo("ikke sant");
    }

}
