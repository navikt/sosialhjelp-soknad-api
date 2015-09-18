package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SendtInnInfoHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        SendtInnInfoHelper helper = new SendtInnInfoHelper();
        handlebars.registerHelper(helper.getNavn(), helper);
    }

    @Test
    public void skalSkriveUtRiktigAntallInnsendteOgPaakrevdeVedlegg() throws IOException {
        WebSoknad webSoknad = lagSoknadMedVedlegg();

        String innhold = handlebars.compileInline("{{#sendtInnInfo}}innsendte: {{sendtInn}}, påkrevde: {{ikkeSendtInn}}{{/sendtInnInfo}}").apply(webSoknad);
        assertThat(innhold).isEqualTo("innsendte: 2, påkrevde: 3");
    }

    @Test
    public void skalSkriveUtRiktigDato() throws IOException {
        WebSoknad webSoknad = new WebSoknad();
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2015, 7, 13, 18, 30).getMillis());

        String innhold = handlebars.compileInline("{{#sendtInnInfo}}innsendt dato: {{innsendtDato}}{{/sendtInnInfo}}").apply(webSoknad);
        assertThat(innhold).isEqualTo("innsendt dato: 13. juli 2015, klokken 18.30");
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