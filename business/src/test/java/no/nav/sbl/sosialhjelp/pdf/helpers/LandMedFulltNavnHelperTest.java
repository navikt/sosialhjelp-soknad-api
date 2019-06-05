package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.kodeverk.Adressekodeverk;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LandMedFulltNavnHelperTest {

    @Test
    public void skalViseFulltNavnPaaLandGittISO3166Forkortelse() throws IOException {
        final Adressekodeverk adressekodeverk = mock(Adressekodeverk.class);
        final Handlebars handlebars = createHandlebarsWithHelper(adressekodeverk);

        when(adressekodeverk.getLand("NOR")).thenReturn("Norge");
        when(adressekodeverk.getLand("SWE")).thenReturn("Sverige");

        String compiled = handlebars.compileInline("{{landMedFulltNavn \"NOR\"}}, {{landMedFulltNavn \"SWE\"}}").apply(new Object());
        assertThat(compiled, is("Norge, Sverige"));
    }

    private Handlebars createHandlebarsWithHelper(Adressekodeverk adressekodeverk) {
        final Handlebars handlebars = new Handlebars();
        final LandMedFulltNavnHelper landMedFulltNavnHelper = new LandMedFulltNavnHelper(adressekodeverk);
        handlebars.registerHelper(landMedFulltNavnHelper.getNavn(), landMedFulltNavnHelper);
        return handlebars;
    }
}