package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;


public class HvisMerHelperTest {

    private Handlebars handlebars;
    private HashMap<Object, Object> model;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HvisMerHelper helper = new HvisMerHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
        model = new HashMap<>();
    }

    @Test
    public void skalViseStorreVedStorreVerdi() throws Exception {
        model.put("verdi", "2");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1\"}}Verdien er høyere{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er høyere");
    }

    @Test
    public void skalViseElseVedLikVerdi() throws Exception {
        model.put("verdi", "1");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1\"}}Verdien er høyere{{else}}Verdien er lik eller mindre{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er lik eller mindre");
    }

    @Test
    public void skalViseElseVedMindreVerdi() throws Exception {
        model.put("verdi", "0");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1\"}}Verdien er høyere{{else}}Verdien er lik eller mindre{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er lik eller mindre");
    }

    @Test
    public void skalViseHoyereVerdiVedHoyereDesimaltallMedKomma() throws Exception {
        model.put("verdi", "1,2");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1,1\"}}Verdien er høyere{{else}}Verdien er lik eller mindre{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er høyere");
    }

    @Test
    public void skalViseElseVedLikDesimaltallMedKomma() throws Exception {
        model.put("verdi", "1,1");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1,1\"}}Verdien er høyere{{else}}Verdien er lik eller mindre{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er lik eller mindre");
    }

    @Test
    public void skalViseElseVedMindreDesimaltallMedKomma() throws Exception {
        model.put("verdi", "1,0");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1,1\"}}Verdien er høyere{{else}}Verdien er lik eller mindre{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er lik eller mindre");
    }

    @Test
    public void skalViseHoyereVerdiVedHoyereDesimaltallMedPunktum() throws Exception {
        model.put("verdi", "1.2");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1,1\"}}Verdien er høyere{{else}}Verdien er lik eller mindre{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er høyere");
    }

    @Test
    public void skalViseElseVedLikDesimaltallMedPunktum() throws Exception {
        model.put("verdi", "1.1");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1,1\"}}Verdien er høyere{{else}}Verdien er lik eller mindre{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er lik eller mindre");
    }

    @Test
    public void skalViseElseVedMindreDesimaltallMedPunktum() throws Exception {
        model.put("verdi", "1.0");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1,1\"}}Verdien er høyere{{else}}Verdien er lik eller mindre{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er lik eller mindre");
    }
}