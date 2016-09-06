package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.Constraint;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.FaktumStruktur;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.TekstStruktur;
import no.nav.sbl.dialogarena.service.oppsummering.OppsummeringsFaktum;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class HvisFaktumstrukturHarInfoteksterHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisFaktumstrukturHarInfoteksterHelper helper = new HvisFaktumstrukturHarInfoteksterHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseBlokkOmFaktumHarInfoteksterDefinertIStrukturMedConstraintsSomOppfylles() throws IOException {
        Faktum contrainedFaktum = new Faktum().medKey("etfaktum").medProperty("prop", "11");
        FaktumStruktur faktumStruktur = new FaktumStruktur();
        TekstStruktur tekstStruktur = new TekstStruktur();
        Constraint constraint = new Constraint("etfaktum", "properties['prop'] > 10");
        WebSoknad webSoknad = new WebSoknad().medFaktum(contrainedFaktum);

        tekstStruktur.setKey("enkey");
        tekstStruktur.setType("infotekst");
        tekstStruktur.setConstraints(Arrays.asList(constraint));

        faktumStruktur.setTekster(Arrays.asList(tekstStruktur));
        Faktum faktum = new Faktum().medKey("hovedfaktum");
        OppsummeringsFaktum oppsummeringsFaktum = new OppsummeringsFaktum(webSoknad, faktumStruktur, faktum, null, null);

        Context parentContext = Context.newContext(webSoknad);
        Context childContext = Context.newContext(parentContext, oppsummeringsFaktum);

        String compiled = handlebars.compileInline("{{#hvisFaktumstrukturHarInfotekster}}harInfoTekster{{/hvisFaktumstrukturHarInfotekster}}").apply(childContext);
        assertThat(compiled).isEqualTo("harInfoTekster");
    }

    @Test
    public void skalIkkeViseBlokkOmFaktumHarInfoteksterDefinertIStrukturMedConstraintsSomIkkeOppfylles() throws IOException {
        Faktum contrainedFaktum = new Faktum().medKey("etfaktum").medProperty("prop", "5");
        FaktumStruktur faktumStruktur = new FaktumStruktur();
        TekstStruktur tekstStruktur = new TekstStruktur();
        Constraint constraint = new Constraint("etfaktum", "properties['prop'] > 10");
        WebSoknad webSoknad = new WebSoknad().medFaktum(contrainedFaktum);

        tekstStruktur.setKey("enkey");
        tekstStruktur.setType("infotekst");
        tekstStruktur.setConstraints(Arrays.asList(constraint));

        faktumStruktur.setTekster(Arrays.asList(tekstStruktur));
        Faktum faktum = new Faktum().medKey("hovedfaktum");
        OppsummeringsFaktum oppsummeringsFaktum = new OppsummeringsFaktum(webSoknad, faktumStruktur, faktum, null, null);

        Context parentContext = Context.newContext(webSoknad);
        Context childContext = Context.newContext(parentContext, oppsummeringsFaktum);

        String compiled = handlebars.compileInline("{{#hvisFaktumstrukturHarInfotekster}}harInfoTekster{{else}}ikkeInfoTekst{{/hvisFaktumstrukturHarInfotekster}}").apply(childContext);
        assertThat(compiled).isEqualTo("ikkeInfoTekst");
    }
}
