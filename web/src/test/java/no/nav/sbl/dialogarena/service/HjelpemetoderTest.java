package no.nav.sbl.dialogarena.service;

import com.github.jknack.handlebars.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class HjelpemetoderTest {

    @Test
    public void testLagItererbarRespons() throws IOException {
        Handlebars handlebars = new Handlebars();

        Context context = Context.newContext(null);
        Template template = handlebars.compileInline("Faktum nr. {{index}}: {{value}} {{odd}}{{even}} {{first}}{{last}}\n");
        Options options = new Options.Builder(handlebars, "dummyHelper", TagType.SECTION, context, template).build();

        ArrayList<Faktum> strings = new ArrayList<>();
        strings.add(new Faktum().medValue("Faktum1"));
        strings.add(new Faktum().medValue("Faktum2"));
        strings.add(new Faktum().medValue("Faktum3"));

        String generated = Hjelpemetoder.lagItererbarRespons(options, strings);
        assertThat(generated).contains("Faktum nr. 0: Faktum1 even first\n" +
                "Faktum nr. 1: Faktum2 odd \n" +
                "Faktum nr. 2: Faktum3 even last\n");
    }

}