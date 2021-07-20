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
public class HvisBarneutgiftHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisBarneutgiftHelper hvisBarneutgiftHelper;

    @Mock
    HentSvaralternativerHelper hentSvaralternativerHelper;

    @BeforeEach
    public void setup() {
        final Set<String> barneutgiftTyper = new HashSet<>();
        barneutgiftTyper.add("barnehage");
        barneutgiftTyper.add("sfo");
        barneutgiftTyper.add("annenBarneutgift");
        when(hentSvaralternativerHelper.findChildPropertySubkeys(anyString(), any(Locale.class))).thenReturn(barneutgiftTyper);
        handlebars = new Handlebars();
        handlebars.registerHelper(hvisBarneutgiftHelper.getNavn(), hvisBarneutgiftHelper);
    }

    @Test
    public void skalGjenkjenneBarneutgiftstype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisBarneutgift \"barnehage\"}}barnehage er en barneutgift{{else}}ikke en barneutgift{{/hvisBarneutgift}}").apply(new Object());

        assertThat(compiled).isEqualTo("barnehage er en barneutgift");
    }

    @Test
    public void skalIkkeGjenkjenneBarneutgiftstype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisBarneutgift \"gretneBarn\"}}gretneBarn er en barneutgift{{else}}ikke en barneutgift{{/hvisBarneutgift}}").apply(new Object());

        assertThat(compiled).isEqualTo("ikke en barneutgift");
    }

}
