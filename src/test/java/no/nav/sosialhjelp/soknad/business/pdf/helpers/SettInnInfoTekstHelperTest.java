package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sosialhjelp.soknad.business.pdf.CmsTekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettInnInfoTekstHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    SettInnInfotekstHelper settInnInfotekstHelper;

    @Mock
    CmsTekst cmsTekst;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(settInnInfotekstHelper.getNavn(), settInnInfotekstHelper);
    }

    @Test
    public void skalHenteInfotekstMedTittel() throws IOException {
        when(cmsTekst.getCmsTekst(eq("testTekst"), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("Lorem ipsum");
        when(cmsTekst.getCmsTekst(eq("infotekst.oppsummering.tittel"), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("Infotekst:");

        String compiled = handlebars.compileInline("{{{settInnInfotekst \"testTekst\"}}}").apply(new Object());

        assertThat(compiled).contains("Infotekst:");
        assertThat(compiled).contains("Lorem ipsum");
    }
    
    @Test
    public void skalReturnereTomStrengHvisIkkeInfotekstFinnes() throws IOException {
        String compiled = handlebars.compileInline("{{{settInnInfotekst \"testTekst\"}}}").apply(new Object());

        assertThat(compiled).isEqualTo("");
    }

}