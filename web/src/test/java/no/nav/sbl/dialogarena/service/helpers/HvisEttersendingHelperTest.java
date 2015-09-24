package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class HvisEttersendingHelperTest {

    private Handlebars handlebars;

    @Before
    public void setup() {
        handlebars = new Handlebars();
        HvisEttersendingHelper hvisEttersending = new HvisEttersendingHelper();
        handlebars.registerHelper(hvisEttersending.getNavn(), hvisEttersending);
    }

    @Test
    public void erSannHvisSoknadErEttersendingOpprettet() throws IOException {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.setDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET);
        String hvisEttersendingKompilert = handlebars.compileInline("{{#hvisEttersending}}Søknad er ettersending{{/hvisEttersending}}").apply(webSoknad);
        assertThat(hvisEttersendingKompilert).isEqualTo("Søknad er ettersending");
    }

    @Test
    public void erFalseHvisVanligSoknad() throws IOException {
        WebSoknad webSoknad = new WebSoknad();
        webSoknad.setDelstegStatus(DelstegStatus.OPPRETTET);
        String hvisEttersendingKompilert = handlebars.compileInline("{{#hvisEttersending}}Soknad er ettersending{{else}}Søknad er opprinnelig søknad{{/hvisEttersending}}").apply(webSoknad);
        assertThat(hvisEttersendingKompilert).isEqualTo("Søknad er opprinnelig søknad");
    }
}
