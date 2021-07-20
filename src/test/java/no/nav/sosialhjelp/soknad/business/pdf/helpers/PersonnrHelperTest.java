package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class PersonnrHelperTest {

    private Handlebars handlebars;


    @BeforeEach
    public void setup() {
        handlebars = new Handlebars();
        PersonnrHelper personnrHelper = new PersonnrHelper();
        handlebars.registerHelper(personnrHelper.getNavn(), personnrHelper);
    }

    @Test
    public void skalHentePersonnr() throws IOException {
        JsonEktefelle ektefelle = new JsonEktefelle();
        ektefelle.setPersonIdentifikator("65432112345"); // Ikke ekte person
        String compiled = handlebars.compileInline("Personnr: {{personnr personIdentifikator }}").apply(ektefelle);

        assertThat(compiled).isEqualTo("Personnr: 12345");
    }

    @Test
    public void skalHenteTomStrengForUgyldigPersonIdentifikator() throws IOException {
        JsonEktefelle ektefelle = new JsonEktefelle();
        ektefelle.setPersonIdentifikator("1231231234");
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