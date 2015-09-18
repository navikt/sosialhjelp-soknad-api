package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;


public class HvisSantHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisSantHelper helper = new HvisSantHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseInnholdVedTrueStreng() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisSant \"true\" }}en sann verdi{{/hvisSant}}").apply(new Object());
        assertThat(compiled).isEqualTo("en sann verdi");
    }

    @Test
    public void skalViseInvertertInnholdVedFalseStreng() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisSant \"nottrue\" }}en sann verdi{{else}}en usann verdi{{/hvisSant}}").apply(new Object());
        assertThat(compiled).isEqualTo("en usann verdi");
    }

    @Test
    public void skalViseInnholdVedTrueVariabel() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("truthy", "true");
        String compiled = handlebars.compileInline("{{#hvisSant truthy }}en sann verdi{{/hvisSant}}").apply(map);
        assertThat(compiled).isEqualTo("en sann verdi");
    }

}