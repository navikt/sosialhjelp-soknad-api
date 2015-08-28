package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;


public class HvisMerHelperTest {

    @Test
    public void skalViseStorreVedStorreVerdi() throws Exception {
        Handlebars handlebars = new Handlebars();
        handlebars.registerHelper(HvisMerHelper.NAVN, HvisMerHelper.INSTANS);

        HashMap<Object, Object> model = new HashMap<>();
        model.put("verdi", "2");
        String compiles = handlebars.compileInline("{{#hvisMer verdi \"1\"}}Verdien er høyere{{/hvisMer}}").apply(Context.newContext(model));
        assertThat(compiles).isEqualTo("Verdien er høyere");

    }
}