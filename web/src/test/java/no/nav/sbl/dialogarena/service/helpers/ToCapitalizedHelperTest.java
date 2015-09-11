package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ToCapitalizedHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(ToCapitalizedHelper.NAVN, ToCapitalizedHelper.INSTANS);
    }

    @Test
    public void skalViseCapitalizedVedVariabel() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("variabel", "crAZy ér KÜlt");
        String innhold = handlebars.compileInline("{{toCapitalized variabel}}").apply(map);
        assertThat(innhold).isEqualTo("Crazy Ér Kült");
    }


    @Test
    public void skalViseSCapitalizedVedTekst() throws IOException {
        String innhold = handlebars.compileInline("{{toCapitalized \"crAZy ér KÜlt\"}}").apply(new Object());
        assertThat(innhold).isEqualTo("Crazy Ér Kült");
    }


}