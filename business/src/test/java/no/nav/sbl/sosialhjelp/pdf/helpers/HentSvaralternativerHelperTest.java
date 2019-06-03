package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Properties;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HentSvaralternativerHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentSvaralternativerHelper hentSvaralternativerHelper;
    
    @Mock
    NavMessageSource navMessageSource;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentSvaralternativerHelper.getNavn(), hentSvaralternativerHelper);
    }


    @Test
    public void skalHenteAlleValg() throws IOException {
        Properties tekstFiler = new Properties();
        leggTilValgtekster(tekstFiler);
        when(navMessageSource.getBundleFor("soknadsosialhjelp", SPRAK)).thenReturn(tekstFiler);
        
        String compiled = handlebars.compileInline("{{{hentSvaralternativer \"bosituasjon\"}}}").apply(new Object());

        assertThat(compiled, containsString("Eier egen bolig"));
        assertThat(compiled, containsString("Leier privat"));
        assertThat(compiled, containsString("Annen bosituasjon"));
    }
    
    @Test
    public void skalFiltrereBortTeksterSomIkkeErValg() throws IOException {
        Properties tekstFiler = new Properties();
        leggTilValgtekster(tekstFiler);
        leggTilTeksterSomIkkeErValgbare(tekstFiler);
        when(navMessageSource.getBundleFor("soknadsosialhjelp", SPRAK)).thenReturn(tekstFiler);
        
        String compiled = handlebars.compileInline("{{{hentSvaralternativer \"bosituasjon\"}}}").apply(new Object());

        assertThat(compiled, containsString("Eier egen bolig"));
        assertThat(compiled, containsString("Leier privat"));
        assertThat(compiled, containsString("Annen bosituasjon"));
        
        assertThat(compiled, not(containsString("En label")));
        assertThat(compiled, not(containsString("Et spørsmål")));
        assertThat(compiled, not(containsString("En feilmelding")));
        assertThat(compiled, not(containsString("En infotekst")));
        assertThat(compiled, not(containsString("En hjelpetekst")));
    }
    
    @Test
    public void skalFiltrereBortUndervalg() throws IOException {
        Properties tekstFiler = new Properties();
        leggTilValgtekster(tekstFiler);
        leggTilUndervalgtekster(tekstFiler);
        when(navMessageSource.getBundleFor("soknadsosialhjelp", SPRAK)).thenReturn(tekstFiler);
        
        String compiled = handlebars.compileInline("{{{hentSvaralternativer \"bosituasjon\"}}}").apply(new Object());

        assertThat(compiled, containsString("Eier egen bolig"));
        assertThat(compiled, containsString("Leier privat"));
        assertThat(compiled, containsString("Annen bosituasjon"));
        
        assertThat(compiled, not(containsString("Bor hos foreldre")));
        assertThat(compiled, not(containsString("Institusjon")));
        assertThat(compiled, not(containsString("Krisesenter")));
    }
    
    private void leggTilValgtekster(Properties tekstFiler) {
        tekstFiler.put("bosituasjon.eier", "Eier egen bolig");
        tekstFiler.put("bosituasjon.leierprivat", "Leier privat");
        tekstFiler.put("bosituasjon.annet", "Annen bosituasjon");
    }


    private void leggTilTeksterSomIkkeErValgbare(Properties tekstFiler) {
        tekstFiler.put("bosituasjon.label", "En label");
        tekstFiler.put("bosituasjon.sporsmal", "Et spørsmal");
        tekstFiler.put("bosituasjon.feilmelding", "En feilmelding");
        tekstFiler.put("bosituasjon.infotekst", "En infotekst");
        tekstFiler.put("bosituasjon.hjelpetekst", "En hjelpetekst");
    }
    
    private void leggTilUndervalgtekster(Properties tekstFiler) {
        tekstFiler.put("bosituasjon.annet.botype.foreldre", "Bor hos foreldre");
        tekstFiler.put("bosituasjon.annet.botype.institusjon", "Institusjon");
        tekstFiler.put("bosituasjon.annet.botype.krisesenter", "Krisesenter");
    }

}