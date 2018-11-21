package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Locale;

import static org.apache.commons.lang3.LocaleUtils.toLocale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

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