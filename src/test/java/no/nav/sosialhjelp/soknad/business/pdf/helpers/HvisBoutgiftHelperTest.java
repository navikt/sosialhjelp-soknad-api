package no.nav.sosialhjelp.soknad.business.pdf.helpers;

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
public class HvisBoutgiftHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisBoutgiftHelper hvisBoutgiftHelper;

    @Mock
    HentSvaralternativerHelper hentSvaralternativerHelper;

    @Before
    public void setup() {
        final Set<String> boutgiftTyper = new HashSet<>();
        boutgiftTyper.add("husleie");
        boutgiftTyper.add("strom");
        boutgiftTyper.add("annenBoutgift");
        when(hentSvaralternativerHelper.findChildPropertySubkeys(anyString(), any(Locale.class))).thenReturn(boutgiftTyper);
        handlebars = new Handlebars();
        handlebars.registerHelper(hvisBoutgiftHelper.getNavn(), hvisBoutgiftHelper);
    }

    @Test
    public void skalGjenkjenneBoutgiftstype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisBoutgift \"husleie\"}}husleie er en boutgift{{else}}ikke en boutgift{{/hvisBoutgift}}").apply(new Object());

        assertThat(compiled, is("husleie er en boutgift"));
    }

    @Test
    public void skalIkkeGjenkjenneBoutgiftstype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisBoutgift \"stygtVeggpanel\"}}stygtVeggpanel er en boutgift{{else}}ikke en boutgift{{/hvisBoutgift}}").apply(new Object());

        assertThat(compiled, is("ikke en boutgift"));
    }

}
