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
public class HvisSparingHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisSparingHelper hvisSparingHelper;

    @Mock
    HentSvaralternativerHelper hentSvaralternativerHelper;

    @BeforeEach
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

        assertThat(compiled).isEqualTo("brukskonto er en sparingstype");
    }

    @Test
    public void skalIkkeGjenkjenneSparingstype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisSparing \"skattekart\"}}skattekart er en sparingstype{{else}}ikke en sparingstype{{/hvisSparing}}").apply(new Object());

        assertThat(compiled).isEqualTo("ikke en sparingstype");
    }

}