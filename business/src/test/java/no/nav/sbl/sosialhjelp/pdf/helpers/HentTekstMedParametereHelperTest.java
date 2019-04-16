package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Properties;

import static no.nav.sbl.dialogarena.common.Spraak.NORSK_BOKMAAL;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HentTekstMedParametereHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentTekstMedParametereHelper hentTekstMedParametereHelper;
    
    @Mock
    NavMessageSource navMessageSource;

    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    private String bundlename = "bundlename";
    private String mittprefix = "mittprefix";

    @Before
    public void setup() {
        KravdialogInformasjon kravdialogInformasjon = mock(KravdialogInformasjon.class);
        when(kravdialogInformasjonHolder.hentKonfigurasjon(anyString())).thenReturn(kravdialogInformasjon);
        when(kravdialogInformasjon.getBundleName()).thenReturn(bundlename);
        when(kravdialogInformasjon.getSoknadTypePrefix()).thenReturn(mittprefix);
        handlebars = new Handlebars();
        handlebars.registerHelper(hentTekstMedParametereHelper.getNavn(), hentTekstMedParametereHelper);
    }

    @Test
    public void hentTekstMedEnParameter() throws IOException {
        String testStreng = "<div>Parameter er satt til: {parameter}.</div>";
        String key = "test";
        lagPropertiesMedTekstOgFilnavnNokkel(testStreng, key);

        String compiled = handlebars.compileInline("{{{hentTekstMedParametere \"" + key + "\" \"parameter\" \"verdi\"}}}").apply(new Object());
        
        assertThat(compiled, is("<div>Parameter er satt til: verdi.</div>"));
    }

    @Test
    public void hentTekstMedFlereParametere() throws IOException {
        String testStreng = "<div>Parametere er satt til: {parameter1}, {parameter2}, {parameter3}.</div>";
        String key = "test";
        lagPropertiesMedTekstOgFilnavnNokkel(testStreng, key);
        
        String compiled = handlebars.compileInline(
                "{{{hentTekstMedParametere \"" + key + "\" \"parameter1\" \"verdi1\" \"parameter2\" \"verdi2\" \"parameter3\" \"verdi3\"}}}")
                .apply(new Object());

        assertThat(compiled, is("<div>Parametere er satt til: verdi1, verdi2, verdi3.</div>"));
    }
    
    @Test
    public void hentTekstMedUfullstendigParameter() throws IOException {
        String testStreng = "<div>Parameter er satt til: {parameter}.</div>";
        String key = "test";
        lagPropertiesMedTekstOgFilnavnNokkel(testStreng, key);
        
        String compiled = handlebars.compileInline("{{{hentTekstMedParametere \"" + key + "\" \"parameter\"}}}").apply(new Object());

        assertThat(compiled, is("<div>Parameter er satt til: {parameter}.</div>"));
    }
    
    @Test
    public void hentTekstUtenParametere() throws IOException {
        String testStreng = "<div>Parameter er satt til: {parameter}.</div>";
        String key = "test";
        lagPropertiesMedTekstOgFilnavnNokkel(testStreng, key);
        
        String compiled = handlebars.compileInline("{{{hentTekstMedParametere \"" + key + "\"}}}").apply(new Object());

        assertThat(compiled, is("<div>Parameter er satt til: {parameter}.</div>"));
    }

    private void lagPropertiesMedTekstOgFilnavnNokkel(String testStreng, String key) {
        Properties properties = new Properties();
        properties.setProperty(mittprefix + "." + key, testStreng);
        when(navMessageSource.getBundleFor(bundlename, NORSK_BOKMAAL)).thenReturn(properties);
    }
    
}