package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ForSortertePropertiesTest {
    private Handlebars handlebars;

    @Before
    public void setup() {
        ForSorterteProperties helper = new ForSorterteProperties();
        handlebars = new Handlebars();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalSortereAlfabetisk() throws IOException {
        Faktum faktum = new Faktum().medProperty("b", "tekstb").medProperty("c", "tekstc").medProperty("a", "teksta");
        String innhold = handlebars.compileInline("{{#forSortertProperties this}}{{value}}, {{/forSortertProperties}}").apply(faktum);
        assertThat(innhold).isEqualTo("teksta, tekstb, tekstc, ");
    }


    @Test
    public void skalSortereTallOgTekstSomTekst() throws IOException {
        Faktum faktum = new Faktum().medProperty("t2", "tekst2").medProperty("t1", "tekst1").medProperty("t11", "tekst11");
        String innhold = handlebars.compileInline("{{#forSortertProperties this}}{{value}}, {{/forSortertProperties}}").apply(faktum);
        assertThat(innhold).isEqualTo("tekst1, tekst11, tekst2, ");
    }

    @Test
    public void skalSortereTekstSomBareErTallNumerisk() throws IOException {
        Faktum faktum = new Faktum().medProperty("2", "tekst2").medProperty("1", "tekst1").medProperty("11", "tekst11") ;
        String innhold = handlebars.compileInline("{{#forSortertProperties this}}{{value}}, {{/forSortertProperties}}").apply(faktum);
        assertThat(innhold).isEqualTo("tekst1, tekst2, tekst11, ");
    }
}