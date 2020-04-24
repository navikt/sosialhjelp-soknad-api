package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettInnKnappTilgjengeligTekstHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    SettInnKnappTilgjengeligTekstHelper settInnKnappTilgjengeligTekstHelper;

    @Mock
    CmsTekst cmsTekst;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(settInnKnappTilgjengeligTekstHelper.getNavn(), settInnKnappTilgjengeligTekstHelper);
    }

    @Test
    public void skalHenteKnapptekstMedTittel() throws IOException {
        when(cmsTekst.getCmsTekst(eq("testTekst"), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("Lorem ipsum");

        String compiled = handlebars.compileInline("{{{settInnKnappTilgjengeligTekst \"testTekst\"}}}").apply(new Object());

        assertThat(compiled, containsString("Knapp tilgjengelig:"));
        assertThat(compiled, containsString("Lorem ipsum"));
    }

    @Test
    public void skalReturnereTomStrengHvisIkkeKnapptekstFinnes() throws IOException {
        String compiled = handlebars.compileInline("{{{settInnKnappTilgjengeligTekst \"testTekst\"}}}").apply(new Object());

        assertThat(compiled, is(""));
    }

}