package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.helpers.foreldrepenger.ForPerioderHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ForPerioderHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        ForPerioderHelper helper = new ForPerioderHelper();
        handlebars = new Handlebars();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalIterereOverPerioderSortertPaaFradato() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.1", "2015-03-03", "2015-03-05", "3"))
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.2", "2015-01-04", "2015-01-05", "1"))
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.3", "2015-02-04", "2015-02-05", "2"))
                .medFaktum(getPeriodeFaktum("perioder.tidsrom.4", "2015-04-04", "2015-04-05", "4"));
        String innhold = handlebars.compileInline("{{#forPerioder}}{{value}}, {{/forPerioder}}").apply(soknad);
        assertThat(innhold).isEqualTo("1, 2, 3, 4, ");
    }

    @Test
    public void skalViseElseOmIngenFakta() throws IOException {
        WebSoknad soknad = new WebSoknad()
                .medFaktum(getPeriodeFaktum("ugyldigkey", "2015-04-04", "2015-04-05", "4"));
        String innhold = handlebars.compileInline("{{#forPerioder}}{{value}}, {{else}}tom{{/forPerioder}}").apply(soknad);
        assertThat(innhold).isEqualTo("tom");
    }


    private Faktum getPeriodeFaktum(String key, String fradato, String tildato, String value) {
        return new Faktum().medKey(key).medProperty("fradato", fradato).medProperty("tildato", tildato).medValue(value);
    }
}