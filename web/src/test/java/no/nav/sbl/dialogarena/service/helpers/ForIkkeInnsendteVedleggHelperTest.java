package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ForIkkeInnsendteVedleggHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        ForIkkeInnsendteVedleggHelper helper = new ForIkkeInnsendteVedleggHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalViseAnnenBeskjedOmIngenIkkeInnsendteVedlegg() throws Exception {
        WebSoknad soknad = new WebSoknad();

        String compiles = handlebars.compileInline("{{#forIkkeInnsendteVedlegg}}ikke innsendt: {{navn}}, {{else}}Ingen ikke-innsendte vedlegg{{/forIkkeInnsendteVedlegg}}").apply(soknad);
        assertThat(compiles).isEqualTo("Ingen ikke-innsendte vedlegg");
    }


    @Test
    public void skalViseIkkeInnsendteVedlegg() throws Exception {
        WebSoknad soknad = lagSoknadMedVedlegg();

        String compiles = handlebars.compileInline("{{#forIkkeInnsendteVedlegg}}ikke innsendt: {{navn}}, {{/forIkkeInnsendteVedlegg}}").apply(soknad);
        assertThat(compiles).isEqualTo("ikke innsendt: ikkeSendtInn1, ikke innsendt: ikkeSendtInn2, ");
    }

    private WebSoknad lagSoknadMedVedlegg() {
        WebSoknad webSoknad = new WebSoknad();

        List<Vedlegg> vedlegg = new ArrayList<>();
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp).medNavn("sendtInn1"));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves).medNavn("ikkeSendtInn1"));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp).medNavn("sendtInn2"));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves).medNavn("ikkeSendtInn2"));
        webSoknad.medVedlegg(vedlegg);

        return webSoknad;
    }

}
