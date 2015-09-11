package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ForInnsendteVedleggHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();
        handlebars.registerHelper(ForInnsendteVedleggHelper.NAVN, ForInnsendteVedleggHelper.INSTANS);
    }

    @Test
    public void skalViseAnnenBeskjedOmIngenInnsendteVedlegg() throws Exception {
        WebSoknad soknad = new WebSoknad();

        String compiles = handlebars.compileInline("{{#forInnsendteVedlegg}}innsendt: {{navn}}, {{else}}Ingen innsendte vedlegg{{/forInnsendteVedlegg}}").apply(soknad);
        assertThat(compiles).isEqualTo("Ingen innsendte vedlegg");
    }


    @Test
    public void skalViseInnsendteVedlegg() throws Exception {
        WebSoknad soknad = lagSoknadMedVedlegg();

        String compiles = handlebars.compileInline("{{#forInnsendteVedlegg}}innsendt: {{navn}}, {{/forInnsendteVedlegg}}").apply(soknad);
        assertThat(compiles).isEqualTo("innsendt: sendtInn1, innsendt: sendtInn2, ");
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
