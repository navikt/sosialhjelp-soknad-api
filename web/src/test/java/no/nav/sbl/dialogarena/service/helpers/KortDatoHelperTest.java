package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class KortDatoHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        KortDatoHelper helper = new KortDatoHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalSkriveUtDatoPaRiktigFormat() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("variabel", "2015-01-31");
        String innhold = handlebars.compileInline("{{kortDato variabel}}").apply(map);
        assertThat(innhold).isEqualTo("31.01.2015");
    }
}