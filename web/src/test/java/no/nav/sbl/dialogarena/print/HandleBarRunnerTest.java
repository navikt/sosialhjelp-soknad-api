package no.nav.sbl.dialogarena.print;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.lowagie.text.DocumentException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.print.helper.JsonTestData.getWebSoknadHtml;
import static no.nav.sbl.dialogarena.print.helper.JsonTestData.getWebSoknadJson;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class HandleBarRunnerTest {

    @Test
    public void runHandlebars() throws IOException {
        Handlebars handlebars = new Handlebars();

        Template template = handlebars.compileInline("Hello {{this}}!");
        String superDev = "SuperDev";
        String appliedToString = template.apply(superDev);

        assertThat(appliedToString, containsString(superDev));
        assertThat(appliedToString, is(equalTo("Hello " + superDev + "!")));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void runMeWithJson() throws IOException {
        String json = getWebSoknadJson();
        Map<String, Object> result = new ObjectMapper().readValue(json, HashMap.class);
        String[] s = {"opprettetDato", "brukerBehandlingId", "status", "aktoerId", "gosysId", "delstegStatus", "fakta", "soknadId"};

        assertThat(result.keySet(), contains(s));
        assertThat((String) result.get("gosysId"), is(equalTo("Dagpenger")));
        assertThat((String) result.get("status"), is(equalTo("UNDER_ARBEID")));

        String applied = HandleBarRunner.applyInline(getWebSoknadHtml(), result);
        assertThat(applied, containsString("Dagpenger"));
        assertThat(applied, containsString("188"));
    }

    @Ignore
    @Test
    public void createPDFFromJson() throws IOException, DocumentException {
        String html = HandleBarRunner.applyTemplate(getWebSoknadJson(), "/html/WebSoknadHtml");
        assertThat(html, containsString("Dagpenger"));
        assertThat(html, containsString("188"));

        String baseUrl = "/c:/test/";
        String pdf = "c:/test/handlebar.pdf";
        PDFCreator.createPDF(html, pdf, baseUrl);

        long start = new Date().getTime();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PDFCreator.createPDF(html, baseUrl, out);
        assertThat(out.size(), is(1155));
        out.close();

        long stop = new Date().getTime();
        long diff = stop - start;
        assertThat(diff, lessThanOrEqualTo(2500l));
    }
}
