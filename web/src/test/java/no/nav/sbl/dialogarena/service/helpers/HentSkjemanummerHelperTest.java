package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class HentSkjemanummerHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        handlebars.registerHelper(HentSkjemanummerHelper.NAVN, HentSkjemanummerHelper.INSTANS);
    }

    @Test
    public void viserSkjemanummer() throws IOException {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.setSkjemaNummer("123456");

        String compiled = handlebars.compileInline("Skjemanummer: {{ hentSkjemanummer }}").apply(webSoknad);
        assertThat(compiled).isEqualTo("Skjemanummer: 123456");
    }


    @Test
    public void viserSkjemaNummerSelvOmWebSoknadIParentContext() throws IOException {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.setSkjemaNummer("999999");

        Context parentContext = Context.newContext(webSoknad);
        Context childContext = Context.newContext(parentContext, "555555");

        String compiled = handlebars.compileInline("Skjemanummer: {{ hentSkjemanummer }}").apply(childContext);
        assertThat(compiled).isEqualTo("Skjemanummer: 999999");
    }


    @Test
    public void viserDagpengerSkjemanummer() throws IOException {
        WebSoknad dagPengeSoknad = lagDagpengeSoknad();

        String compiled = handlebars.compileInline("Skjemanummer: {{ hentSkjemanummer }}").apply(dagPengeSoknad);
        assertThat(compiled).isEqualTo("Skjemanummer: NAV 04-01.04");
    }

    private WebSoknad lagDagpengeSoknad() {
        WebSoknad dagPengeSoknad = new WebSoknad();
        dagPengeSoknad.setSkjemaNummer("NAV 04-01.03");

        Faktum permitering = new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("arbeidsforhold.permitteringsperiode");
        permitering.medProperty("permiteringsperiodedatofra", "2000-01-01");
        dagPengeSoknad.setFakta(Collections.singletonList(permitering));

        return dagPengeSoknad;
    }


}