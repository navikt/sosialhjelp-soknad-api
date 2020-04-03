package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HvisBarneutgiftHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisBarneutgiftHelper hvisBarneutgiftHelper;

    @Mock
    HentSvaralternativerHelper hentSvaralternativerHelper;

    @Before
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

        assertThat(compiled, is("barnehage er en barneutgift"));
    }

    @Test
    public void skalIkkeGjenkjenneBarneutgiftstype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisBarneutgift \"gretneBarn\"}}gretneBarn er en barneutgift{{else}}ikke en barneutgift{{/hvisBarneutgift}}").apply(new Object());

        assertThat(compiled, is("ikke en barneutgift"));
    }

}
