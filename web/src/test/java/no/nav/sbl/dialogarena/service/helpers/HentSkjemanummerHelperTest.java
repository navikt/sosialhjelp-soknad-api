package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class HentSkjemanummerHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        HentSkjemanummerHelper helper = new HentSkjemanummerHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
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
    public void triggerDagpengerLogikkSomOppdatererSkjemanummer() throws IOException {
        WebSoknad dagpengeSoknad = lagDagpengeSoknadMedPermittering(DagpengerUtils.DAGPENGER);

        String compiled = handlebars.compileInline("Skjemanummer: {{ hentSkjemanummer }}").apply(dagpengeSoknad);
        assertThat(compiled).isEqualTo("Skjemanummer: " + DagpengerUtils.DAGPENGER_VED_PERMITTERING);
    }

    private WebSoknad lagDagpengeSoknadMedPermittering(String skjemanummer) {
        WebSoknad dagpengeSoknad = new WebSoknad();
        dagpengeSoknad.setSkjemaNummer(skjemanummer);

        // bruk permitteringsfaktum for å trigge endring av skjemanummer
        Faktum permittering = new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("arbeidsforhold.permitteringsperiode");
        dagpengeSoknad.setFakta(Collections.singletonList(permittering));

        return dagpengeSoknad;
    }


}