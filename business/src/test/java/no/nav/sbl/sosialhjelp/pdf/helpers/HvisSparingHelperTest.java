package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HvisSparingHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisSparingHelper hvisSparingHelper;

    @Mock
    HentSvaralternativerHelper hentSvaralternativerHelper;

    @Before
    public void setup() {
        final Set<String> sparingTyper = new HashSet<>();
        sparingTyper.add("brukskonto");
        sparingTyper.add("bsu");
        sparingTyper.add("sparekonto");
        when(hentSvaralternativerHelper.findChildPropertySubkeys(anyString(), any(Locale.class))).thenReturn(sparingTyper);
        handlebars = new Handlebars();
        handlebars.registerHelper(hvisSparingHelper.getNavn(), hvisSparingHelper);
    }

    @Test
    public void skalGjenkjenneSparingstype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisSparing \"brukskonto\"}}brukskonto er en sparingstype{{else}}ikke en sparingstype{{/hvisSparing}}").apply(new Object());

        assertThat(compiled, is("brukskonto er en sparingstype"));
    }

    @Test
    public void skalIkkeGjenkjenneSparingstype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisSparing \"skattekart\"}}skattekart er en sparingstype{{else}}ikke en sparingstype{{/hvisSparing}}").apply(new Object());

        assertThat(compiled, is("ikke en sparingstype"));
    }

}