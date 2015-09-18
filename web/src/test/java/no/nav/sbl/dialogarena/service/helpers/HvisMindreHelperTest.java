package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class HvisMindreHelperTest {

    private Handlebars handlebars;
    private HashMap<Object, Object> model;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisMindreHelper helper = new HvisMindreHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
        model = new HashMap<>();
    }

    @Test
    public void skalViseMindreVedMindreVerdi() throws Exception {
        model.put("verdi", "49");
        String compiles = handlebars.compileInline("{{#hvisMindre verdi \"50\"}}Verdien er mindre{{/hvisMindre}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er mindre");
    }

    @Test
    public void skalViseElseVedHoyereVerdi() throws Exception {
        model.put("verdi", "51");
        String compiles = handlebars.compileInline("{{#hvisMindre verdi \"50\"}}Verdien er mindre{{else}}Verdien er høyere{{/hvisMindre}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er høyere");
    }

    @Test
    public void skalViseElseVedLikVerdi() throws Exception {
        model.put("verdi", "50");
        String compiles = handlebars.compileInline("{{#hvisMindre verdi \"50\"}}Verdien er mindre{{else}}Verdien er høyere{{/hvisMindre}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er høyere");
    }

}
