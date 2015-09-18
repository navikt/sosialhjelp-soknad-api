package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class FnrTilKortDatoHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        FnrTilKortDatoHelper helper = new FnrTilKortDatoHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalSkriveUtDatoPaRiktigFormat() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("variabel", "27108034322");
        String innhold = handlebars.compileInline("{{fnrTilKortDato variabel}}").apply(map);
        assertThat(innhold).isEqualTo("27.10.1980");
    }
}