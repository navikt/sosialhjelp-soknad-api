package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SettInnInfoTekstHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    SettInnInfotekstHelper settInnInfotekstHelper;

    @Mock
    CmsTekst cmsTekst;

    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Before
    public void setup() {
        KravdialogInformasjon kravdialogInformasjon = mock(KravdialogInformasjon.class);
        when(kravdialogInformasjonHolder.hentKonfigurasjon(anyString())).thenReturn(kravdialogInformasjon);
        when(kravdialogInformasjon.getBundleName()).thenReturn("bundlename");
        when(kravdialogInformasjon.getSoknadTypePrefix()).thenReturn("mittprefix");

        handlebars = new Handlebars();
        handlebars.registerHelper(settInnInfotekstHelper.getNavn(), settInnInfotekstHelper);
    }

    @Test
    public void skalHenteInfotekstMedTittel() throws IOException {
        when(cmsTekst.getCmsTekst(eq("testTekst"), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("Lorem ipsum");
        when(cmsTekst.getCmsTekst(eq("infotekst.oppsummering.tittel"), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("Infotekst:");

        String compiled = handlebars.compileInline("{{{settInnInfotekst \"testTekst\"}}}").apply(new Object());

        assertThat(compiled, containsString("Infotekst:"));
        assertThat(compiled, containsString("Lorem ipsum"));
    }
    
    @Test
    public void skalReturnereTomStrengHvisIkkeInfotekstFinnes() throws IOException {
        when(cmsTekst.getCmsTekst(eq("infotekst.oppsummering.tittel"), any(Object[].class), anyString(), anyString(), any(Locale.class))).thenReturn("Infotekst:");

        String compiled = handlebars.compileInline("{{{settInnInfotekst \"testTekst\"}}}").apply(new Object());

        assertThat(compiled, is(""));
    }

}