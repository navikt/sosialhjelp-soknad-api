package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ToLowerCaseHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        ToLowerCaseHelper helper = new ToLowerCaseHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseSmaaBokstaverVedVariabel() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("variabel", "CONTENT");
        String innhold = handlebars.compileInline("{{toLowerCase variabel}}").apply(map);
        assertThat(innhold).isEqualTo("content");
    }


    @Test
    public void skalViseSmaaBokstaverVedTekst() throws IOException {
        String innhold = handlebars.compileInline("{{toLowerCase \"MaSSe Case\"}}").apply(new Object());
        assertThat(innhold).isEqualTo("masse case");
    }


}