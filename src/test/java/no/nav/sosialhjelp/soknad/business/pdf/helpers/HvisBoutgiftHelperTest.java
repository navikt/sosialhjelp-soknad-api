package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HvisBoutgiftHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisBoutgiftHelper hvisBoutgiftHelper;

    @Mock
    HentSvaralternativerHelper hentSvaralternativerHelper;

    @BeforeEach
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

        assertThat(compiled).isEqualTo("husleie er en boutgift");
    }

    @Test
    public void skalIkkeGjenkjenneBoutgiftstype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisBoutgift \"stygtVeggpanel\"}}stygtVeggpanel er en boutgift{{else}}ikke en boutgift{{/hvisBoutgift}}").apply(new Object());

        assertThat(compiled).isEqualTo("ikke en boutgift");
    }

}
