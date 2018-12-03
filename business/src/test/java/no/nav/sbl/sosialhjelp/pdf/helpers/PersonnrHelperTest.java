package no.nav.sbl.sosialhjelp.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class PersonnrHelperTest {

    private Handlebars handlebars;


    @Before
    public void setup() {
        handlebars = new Handlebars();
        PersonnrHelper personnrHelper = new PersonnrHelper();
        handlebars.registerHelper(personnrHelper.getNavn(), personnrHelper);
    }

    @Test
    public void skalHentePersonnr() throws IOException {
        JsonEktefelle ektefelle = new JsonEktefelle();
        ektefelle.setPersonIdentifikator("***REMOVED***");
        String compiled = handlebars.compileInline("Personnr: {{personnr personIdentifikator }}").apply(ektefelle);

        assertThat(compiled).isEqualTo("Personnr: 12345");
    }
    
    @Test
    public void skalHenteTomStrengForUgyldigPersonIdentifikator() throws IOException {
        JsonEktefelle ektefelle = new JsonEktefelle();
        ektefelle.setPersonIdentifikator("1505951234");
        String compiled = handlebars.compileInline("Personnr: {{personnr personIdentifikator }}").apply(ektefelle);

        assertThat(compiled).isEqualTo("Personnr: ");
        
        ektefelle.setPersonIdentifikator("");
        compiled = handlebars.compileInline("Personnr: {{personnr personIdentifikator }}").apply(ektefelle);

        assertThat(compiled).isEqualTo("Personnr: ");
    }
    
    @Test
    public void skalHenteTomStrengForPersonIdentifikatorLikNull() throws IOException {
        JsonEktefelle ektefelle = new JsonEktefelle();
        ektefelle.setPersonIdentifikator(null);
        String compiled = handlebars.compileInline("Personnr: {{personnr personIdentifikator }}").apply(ektefelle);

        assertThat(compiled).isEqualTo("Personnr: ");
    }

}