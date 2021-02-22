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
public class HvisOkonomiskVerdiHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HvisOkonomiskVerdiHelper hvisOkonomiskVerdiHelper;

    @Mock
    HentSvaralternativerHelper hentSvaralternativerHelper;

    @Before
    public void setup() {
        final Set<String> okonomiskVerdiTyper = new HashSet<>();
        okonomiskVerdiTyper.add("campingvogn");
        okonomiskVerdiTyper.add("kjoretoy");
        okonomiskVerdiTyper.add("bolig");
        when(hentSvaralternativerHelper.findChildPropertySubkeys(anyString(), any(Locale.class))).thenReturn(okonomiskVerdiTyper);
        handlebars = new Handlebars();
        handlebars.registerHelper(hvisOkonomiskVerdiHelper.getNavn(), hvisOkonomiskVerdiHelper);
    }

    @Test
    public void skalGjenkjenneOkonomiskVerditype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisOkonomiskVerdi \"campingvogn\"}}campingvogn er en økonomisk verdi{{else}}ikke en økonomisk verdi{{/hvisOkonomiskVerdi}}").apply(new Object());

        assertThat(compiled, is("campingvogn er en økonomisk verdi"));
    }

    @Test
    public void skalIkkeGjenkjenneOkonomiskVerditype() throws IOException {
        String compiled = handlebars.compileInline("{{#hvisOkonomiskVerdi \"stygtVeggpanel\"}}stygtVeggpanel er en økonomisk verdi{{else}}ikke en økonomisk verdi{{/hvisOkonomiskVerdi}}").apply(new Object());

        assertThat(compiled, is("ikke en økonomisk verdi"));
    }

}
