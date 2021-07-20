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
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.BUNDLE_NAME;
import static no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SosialhjelpInformasjon.SOKNAD_TYPE_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HentTekstMedParametereHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentTekstMedParametereHelper hentTekstMedParametereHelper;
    
    @Mock
    NavMessageSource navMessageSource;

    @BeforeEach
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentTekstMedParametereHelper.getNavn(), hentTekstMedParametereHelper);
    }

    @Test
    void hentTekstMedEnParameter() throws IOException {
        final String testStreng = "<div>Parameter er satt til: {parameter}.</div>";
        final String key = "test";
        lagPropertiesMedTekstOgFilnavnNokkel(testStreng, key);

        String compiled = handlebars.compileInline("{{{hentTekstMedParametere \"" + key + "\" \"parameter\" \"verdi\"}}}").apply(new Object());
        
        assertThat(compiled).isEqualTo("<div>Parameter er satt til: verdi.</div>");
    }

    @Test
    void hentTekstMedFlereParametere() throws IOException {
        final String testStreng = "<div>Parametere er satt til: {parameter1}, {parameter2}, {parameter3}.</div>";
        final String key = "test";
        lagPropertiesMedTekstOgFilnavnNokkel(testStreng, key);
        
        String compiled = handlebars.compileInline(
                "{{{hentTekstMedParametere \"" + key + "\" \"parameter1\" \"verdi1\" \"parameter2\" \"verdi2\" \"parameter3\" \"verdi3\"}}}")
                .apply(new Object());

        assertThat(compiled).isEqualTo("<div>Parametere er satt til: verdi1, verdi2, verdi3.</div>");
    }
    
    @Test
    void hentTekstMedUfullstendigParameter() throws IOException {
        final String testStreng = "<div>Parameter er satt til: {parameter}.</div>";
        final String key = "test";
        lagPropertiesMedTekstOgFilnavnNokkel(testStreng, key);
        
        String compiled = handlebars.compileInline("{{{hentTekstMedParametere \"" + key + "\" \"parameter\"}}}").apply(new Object());

        assertThat(compiled).isEqualTo("<div>Parameter er satt til: {parameter}.</div>");
    }
    
    @Test
    void hentTekstUtenParametere() throws IOException {
        final String testStreng = "<div>Parameter er satt til: {parameter}.</div>";
        final String key = "test";
        lagPropertiesMedTekstOgFilnavnNokkel(testStreng, key);
        
        String compiled = handlebars.compileInline("{{{hentTekstMedParametere \"" + key + "\"}}}").apply(new Object());

        assertThat(compiled).isEqualTo("<div>Parameter er satt til: {parameter}.</div>");
    }

    private void lagPropertiesMedTekstOgFilnavnNokkel(String testStreng, String key) {
        final Properties properties = new Properties();
        properties.setProperty(SOKNAD_TYPE_PREFIX + "." + key, testStreng);
        when(navMessageSource.getBundleFor(BUNDLE_NAME, SPRAK)).thenReturn(properties);
    }
    
}