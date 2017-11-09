package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HentNavEnhetNavnHelperTest {

    private Handlebars handlebars;

    @InjectMocks
    HentNavEnhetNavnHelper hentNavEnhetNavnHelper;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        handlebars.registerHelper(hentNavEnhetNavnHelper.getNavn(), hentNavEnhetNavnHelper);
    }

    @Test
    public void viserNavnetPaaNavEnhetenMedKommune() throws IOException {
        WebSoknad webSoknad = new WebSoknad().medFaktum(new Faktum().medKey("personalia.kommune").medValue("horten"));
        String compiled = handlebars.compileInline("{{hentNavEnhetNavn}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("NAV Horten");
    }

    @Test
    public void viserNavnetPaaNavEnhetMedBydel() throws IOException {
        WebSoknad webSoknad = new WebSoknad()
                .medFaktum(new Faktum().medKey("personalia.kommune").medValue("oslo"))
                .medFaktum(new Faktum().medKey("personalia.bydel").medValue("frogner"));

        String compiled = handlebars.compileInline("{{hentNavEnhetNavn}}").apply(webSoknad);
        assertThat(compiled).isEqualTo("NAV Frogner");
    }
}
