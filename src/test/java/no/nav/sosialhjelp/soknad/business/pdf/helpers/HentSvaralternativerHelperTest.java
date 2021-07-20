package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Properties;

import static no.nav.sosialhjelp.soknad.business.pdf.HandlebarContext.SPRAK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HentSvaralternativerHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    private HentSvaralternativerHelper hentSvaralternativerHelper;
    
    @Mock
    private NavMessageSource navMessageSource;

    @BeforeEach
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentSvaralternativerHelper.getNavn(), hentSvaralternativerHelper);
    }


    @Test
    void skalHenteAlleValg() throws IOException {
        Properties tekstFiler = new Properties();
        leggTilValgtekster(tekstFiler);
        when(navMessageSource.getBundleFor("soknadsosialhjelp", SPRAK)).thenReturn(tekstFiler);
        
        String compiled = handlebars.compileInline("{{{hentSvaralternativer \"bosituasjon\"}}}").apply(new Object());

        assertThat(compiled).contains("Eier egen bolig");
        assertThat(compiled).contains("Leier privat");
        assertThat(compiled).contains("Annen bosituasjon");
    }
    
    @Test
    void skalFiltrereBortTeksterSomIkkeErValg() throws IOException {
        Properties tekstFiler = new Properties();
        leggTilValgtekster(tekstFiler);
        leggTilTeksterSomIkkeErValgbare(tekstFiler);
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
    void skalFiltrereBortUndervalg() throws IOException {
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