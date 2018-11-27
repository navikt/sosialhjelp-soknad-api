package no.nav.sbl.sosialhjelp.pdf.helpers;

import static no.nav.sbl.sosialhjelp.pdf.HandlebarContext.SPRAK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.jknack.handlebars.Handlebars;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;

@RunWith(MockitoJUnitRunner.class)
public class HentSvaralternativerHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentSvaralternativerHelper hentSvaralternativerHelper;
    
    @Mock
    NavMessageSource navMessageSource;

    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    @Before
    public void setup() {
        KravdialogInformasjon kravdialogInformasjon = mock(KravdialogInformasjon.class);
        when(kravdialogInformasjonHolder.hentKonfigurasjon(anyString())).thenReturn(kravdialogInformasjon);
        when(kravdialogInformasjon.getSoknadTypePrefix()).thenReturn("mittprefix");

        handlebars = new Handlebars();
        handlebars.registerHelper(hentSvaralternativerHelper.getNavn(), hentSvaralternativerHelper);
    }


    @Test
    public void skalHenteAlleValg() throws IOException {
        Properties tekstFiler = new Properties();
        leggTilValgtekster(tekstFiler);
        when(navMessageSource.getBundleFor("soknadsosialhjelp", SPRAK)).thenReturn(tekstFiler);
        
        String compiled = handlebars.compileInline("{{{hentSvaralternativer \"bosituasjon\"}}}").apply(new Object());
        
        assertThat(compiled).contains("Eier egen bolig");
        assertThat(compiled).contains("Leier privat");
        assertThat(compiled).contains("Annen bosituasjon");
    }
    
    @Test
    public void skalFiltrereBortTeksterSomIkkeErValg() throws IOException {
        Properties tekstFiler = new Properties();
        leggTilValgtekster(tekstFiler);
        leggTilTeksterSomIkkeErValg(tekstFiler);
        when(navMessageSource.getBundleFor("soknadsosialhjelp", SPRAK)).thenReturn(tekstFiler);
        
        String compiled = handlebars.compileInline("{{{hentSvaralternativer \"bosituasjon\"}}}").apply(new Object());
        
        assertThat(compiled).contains("Eier egen bolig");
        assertThat(compiled).contains("Leier privat");
        assertThat(compiled).contains("Annen bosituasjon");
        
        assertThat(compiled).doesNotContain("En label");
        assertThat(compiled).doesNotContain("Et spørsmål");
        assertThat(compiled).doesNotContain("En feilmelding");
        assertThat(compiled).doesNotContain("En infotekst");
        assertThat(compiled).doesNotContain("En hjelpetekst");
    }
    
    @Test
    public void skalFiltrereBortUndervalg() throws IOException {
        Properties tekstFiler = new Properties();
        leggTilValgtekster(tekstFiler);
        leggTilUndervalgtekster(tekstFiler);
        when(navMessageSource.getBundleFor("soknadsosialhjelp", SPRAK)).thenReturn(tekstFiler);
        
        String compiled = handlebars.compileInline("{{{hentSvaralternativer \"bosituasjon\"}}}").apply(new Object());
        
        assertThat(compiled).contains("Eier egen bolig");
        assertThat(compiled).contains("Leier privat");
        assertThat(compiled).contains("Annen bosituasjon");
        
        assertThat(compiled).doesNotContain("Bor hos foreldre");
        assertThat(compiled).doesNotContain("Institusjon");
        assertThat(compiled).doesNotContain("Krisesenter");
    }
    
    private void leggTilValgtekster(Properties tekstFiler) {
        tekstFiler.put("bosituasjon.eier", "Eier egen bolig");
        tekstFiler.put("bosituasjon.leierprivat", "Leier privat");
        tekstFiler.put("bosituasjon.annet", "Annen bosituasjon");
    }


    private void leggTilTeksterSomIkkeErValg(Properties tekstFiler) {
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