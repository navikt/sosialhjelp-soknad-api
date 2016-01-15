package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class HentFaktumValueHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        HentFaktumValueHelper helper = new HentFaktumValueHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalFinneRiktigFaktumOgReturnereVerdien() throws IOException {
        WebSoknad webSoknad = new WebSoknad().medFaktum(
                new Faktum().medKey("min.key").medValue("min value"));

        String innhold = handlebars.compileInline("{{hentFaktumValue \"min.key\"}}").apply(webSoknad);
        assertThat(innhold).isEqualTo("min value");
    }

}