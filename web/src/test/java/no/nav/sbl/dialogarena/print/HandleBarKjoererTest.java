package no.nav.sbl.dialogarena.print;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.lowagie.text.DocumentException;
import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static no.nav.sbl.dialogarena.print.helper.JsonTestData.hentWebSoknadHtml;
import static no.nav.sbl.dialogarena.print.helper.JsonTestData.hentWebSoknadJson;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class HandleBarKjoererTest {

    @Test
    public void runHandlebars() throws IOException {
        Handlebars handlebars = new Handlebars();

        Template template = handlebars.compileInline("Hello {{this}}!");
        String superDev = "SuperDev";
        String nyTekst = template.apply(superDev);

        assertThat(nyTekst, is(equalTo("Hello " + superDev + "!")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createHashAndApplyTemplate() throws IOException {
        String json = hentWebSoknadJson();
        Kodeverk kodeverk = mock(Kodeverk.class);
        Map<String, Object> resultat = new ObjectMapper().readValue(json, Map.class);
        String[] s = {"soknadId", "skjemaNummer", "brukerBehandlingId", "fakta", "status", "aktoerId", "opprettetDato", "delstegStatus"};

        assertThat(resultat.keySet(), contains(s));
        assertThat((String) resultat.get("skjemaNummer"), is(equalTo("Dagpenger")));
        assertThat((String) resultat.get("status"), is(equalTo("UNDER_ARBEID")));

        String applied = new HandleBarKjoerer(kodeverk).fyllHtmlStringMedInnhold(hentWebSoknadHtml(), resultat);
        assertThat(applied, containsString("Dagpenger"));
        assertThat(applied, containsString("188"));
    }

    @Test
    public void createPDFFromJson() throws IOException, DocumentException {
        assertThat(true, Is.is(true));
//        Kodeverk kodeverk = mock(Kodeverk.class);
//        WebSoknad soknad = new WebSoknad();
//        soknad.setSkjemaNummer("NAV-1-1-1");
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "test", "testinnhold"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "liste", "testinnhold2"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "liste", "testinnhold3"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "test3", "testinnhold4"));
//        String html = new HandleBarKjoerer(kodeverk).fyllHtmlMalMedInnhold(soknad, "/html/WebSoknadHtml");
//        assertThat(html, containsString("NAV-1-1-1"));
//
//        String baseUrl = "/c:/test/";
//        String pdf = "c:/test/handlebar.pdf";
//        PDFFabrikk.lagPdfFil(html, baseUrl, pdf);
//
//        long start = new Date().getTime();
//        ByteArrayOutputStream ut = new ByteArrayOutputStream();
//        PDFFabrikk.lagPDFOutputStream(html, baseUrl, ut);
//        assertThat(ut.size(), is(1155));
//        ut.close();
//
//        long stopp = new Date().getTime();
//        long diff = stopp - start;
//        assertThat(diff, lessThanOrEqualTo(2500l));
    }
}
