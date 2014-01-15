package no.nav.sbl.dialogarena.print;

import com.lowagie.text.DocumentException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

public class DagpengerTest {

    @Test
    public void createPDFFromJson() throws IOException, DocumentException {
        WebSoknad soknad = new WebSoknad();
        soknad.setGosysId("NAV-1-1-1");
        soknad.leggTilFaktum(new Faktum(1L, 1L, "personalia", "true").medProperty("navn", "Ola Normann").medProperty("fnr", "01010101011").medProperty("addresse", "osloveien 1,  0479 oslo"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "reelarbeidsoker.villigdeltid", "true"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "reelarbeidsoker.villighelse", "true"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "reelarbeidsoker.villigdeltid", "true"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "reelarbeidsoker.villigpendle", "true"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "ikkeavtjentverneplikt", "true"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "ikkeegennaering", "true"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "andreytelser.stonadFisker", "true"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "andreytelser.privatTjenestepensjon", "false"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "andreytelser.vartpenger", "true"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "arbeidsforhold", "").medProperty("navn", "arbeidsforhold 1"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "arbeidsforhold", "").medProperty("navn", "arbeidsforhold 2"));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "arbeidsforhold", "").medProperty("navn", "arbeidsforhold 3"));

        String html = HandleBarKjoerer.fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger");

        IOUtils.write(html, new FileOutputStream("/c:/test/dagpenger.html"));

        String baseUrl = "/c:/test/";
        String pdf = "c:/test/dagpenger.pdf";
        PDFFabrikk.lagPdfFil(html, baseUrl, pdf);
        assertThat(html, containsString("NAV-1-1-1"));

        long start = new Date().getTime();
        ByteArrayOutputStream ut = new ByteArrayOutputStream();
        PDFFabrikk.lagPDFOutputStream(html, baseUrl, ut);
        assertThat(ut.size(), is(1155));
        ut.close();

        long stopp = new Date().getTime();
        long diff = stopp - start;
        assertThat(diff, lessThanOrEqualTo(2500l));
    }
}
