package no.nav.sbl.dialogarena.service.helpers.refusjondagligreise;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LagKjorelisteUkerTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        LagKjorelisteUker helper = new LagKjorelisteUker();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalIterereOverUkerForSokteDager() throws IOException {
        Faktum faktum = new Faktum();
        Map<String, String> properties = new HashMap<>();
        properties.put("fom", "2015-12-30");
        properties.put("tom", "2016-01-12");

        properties.put("2015-12-30.soker", "true");
        properties.put("2015-12-31.soker", "true");
        properties.put("2015-01-02.soker", "false");
        properties.put("2016-01-11.soker", "true");

        faktum.setProperties(properties);

        String innhold = handlebars.compileInline("{{#lagKjorelisteUker properties}}uke: {{ukeNr}}, {{/lagKjorelisteUker}}").apply(faktum);
        assertThat(innhold).isEqualTo("uke: 53, uke: 2, ");
    }


    @Test
    public void skalIterereOverDagerOgViseParkeringRiktig() throws IOException {
        Faktum faktum = new Faktum();
        Map<String, String> properties = new HashMap<>();
        properties.put("fom", "2015-12-27");
        properties.put("tom", "2016-01-12");

        properties.put("2015-12-28.soker", "true");
        properties.put("2015-12-28.parkering", "50");

        properties.put("2015-12-29.soker", "false");
        properties.put("2015-12-29.parkering", "20");

        properties.put("2015-12-30.soker", "true");
        properties.put("2015-12-30.parkering", "0");

        properties.put("2015-12-31.soker", "true");

        faktum.setProperties(properties);

        String innhold = handlebars.compileInline("{{#lagKjorelisteUker properties}}{{#each dager}}{{dato}}: {{parkering}}, {{/each}}{{/lagKjorelisteUker}}").apply(faktum);
        assertThat(innhold).isEqualTo("2015-12-28: 50, 2015-12-30: 0, 2015-12-31: 0, ");
    }

}