package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ForVedleggHelperTest {

    private Handlebars handlebars;

    @Before
    public void setUp() throws Exception {
        handlebars = new Handlebars();

        ForVedleggHelper helper = new ForVedleggHelper();
        handlebars.registerHelper(helper.getNavn(), helper);

        HvisLikHelper hvisLikHelper = new HvisLikHelper();
        handlebars.registerHelper(hvisLikHelper.getNavn(), hvisLikHelper);
    }

    @Test
    public void skalViseAnnenBeskjedOmIngenVedlegg() throws Exception {
        WebSoknad soknad = new WebSoknad();

        String compiles = handlebars.compileInline("{{#forVedlegg}}vedlegg{{else}}Ingen vedlegg{{/forVedlegg}}").apply(soknad);
        assertThat(compiles).isEqualTo("Ingen vedlegg");
    }


    @Test
    public void skalIkkeViseVedleggSomIkkeErPaakrevd() throws Exception {
        WebSoknad soknad = new WebSoknad().medVedlegg(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.IkkeVedlegg));

        String compiles = handlebars.compileInline("{{#forVedlegg}}vedlegg: {{navn}}{{else}}Ingen vedlegg{{/forVedlegg}}").apply(soknad);
        assertThat(compiles).isEqualTo("Ingen vedlegg");
    }


    @Test
    public void skalViseVedlegg() throws Exception {
        WebSoknad soknad = lagSoknadMedVedlegg();

        String compiles = handlebars.compileInline("{{#forVedlegg}}vedlegg: {{navn}}{{#hvisLik innsendingsvalg \"LastetOpp\"}} lastet opp, {{ else }} ikke lastet opp, {{/hvisLik}}{{else}}Ingen vedlegg{{/forVedlegg}}").apply(soknad);
        assertThat(compiles).isEqualTo("vedlegg: sendtInn1 lastet opp, vedlegg: ikkeSendtInn1 ikke lastet opp, vedlegg: sendtInn2 lastet opp, ");
    }

    private WebSoknad lagSoknadMedVedlegg() {
        WebSoknad webSoknad = new WebSoknad();

        List<Vedlegg> vedlegg = new ArrayList<>();
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp).medNavn("sendtInn1"));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.VedleggKreves).medNavn("ikkeSendtInn1"));
        vedlegg.add(new Vedlegg().medInnsendingsvalg(Vedlegg.Status.LastetOpp).medNavn("sendtInn2"));
        webSoknad.medVedlegg(vedlegg);

        return webSoknad;
    }


}
