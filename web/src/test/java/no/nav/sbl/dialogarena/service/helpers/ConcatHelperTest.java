package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ConcatHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        ConcatHelper helper = new ConcatHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalLeggeSammenToTekststrenger() throws IOException {
        String innhold = handlebars.compileInline("{{concat \"mye\" \"innhold\"}}").apply(new Object());
        assertThat(innhold).isEqualTo("myeinnhold");
    }

    @Test
    public void skalLeggeSammenToVariabler() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("forsteString", "con");
        map.put("andreString", "tent");
        String innhold = handlebars.compileInline("{{concat forsteString andreString}}").apply(map);
        assertThat(innhold).isEqualTo("content");
    }

    @Test
    public void skalLeggeSammenFlereTekststrenger() throws IOException {
        String innhold = handlebars.compileInline("{{concat \"a\" \"b\" \"c\" \"d\" \"e\" \"f\"}}").apply(new Object());
        assertThat(innhold).isEqualTo("abcdef");
    }


}