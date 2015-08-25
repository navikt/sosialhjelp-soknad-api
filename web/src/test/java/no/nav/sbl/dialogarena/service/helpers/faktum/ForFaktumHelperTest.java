package no.nav.sbl.dialogarena.service.helpers.faktum;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import no.nav.sbl.dialogarena.service.HandlebarRegistry;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ForFaktumHelperTest {
    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        ForFaktumHelper helpers = new ForFaktumHelper();
        ReflectionTestUtils.setField(helpers, "handlebarsRegistry", new HandlebarRegistry() {
            @Override
            public void registrerHelper(String name, Helper helper) {
                handlebars.registerHelper(name, helper);
            }
        });
        helpers.registrer();
    }

    @Test
    public void skalFinneFaktumOgSettePaaContext() throws IOException {
        String compiled = handlebars.compileInline("{{#forFaktum \"test\" }}faktum finnes med verdi {{value}}{{else}}finnes ikke{{/forFaktum}}").apply(Context.newContext(new WebSoknad().medFaktum(new Faktum().medKey("test").medValue("testverdi"))));
        assertThat(compiled).isEqualTo("faktum finnes med verdi testverdi");
    }

    @Test
    public void skalBrukeInverseOmFaktumIkkeFinnes() throws IOException {
        String compiled = handlebars.compileInline("{{#forFaktum \"test\" }}faktum finnes med verdi {{value}}{{else}}finnes ikke{{/forFaktum}}").apply(Context.newContext(new WebSoknad()));
        assertThat(compiled).isEqualTo("finnes ikke");
    }

}