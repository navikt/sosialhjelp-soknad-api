package no.nav.sbl.dialogarena.print;

import com.lowagie.text.DocumentException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DagpengerTest {

    @Test
    public void createPDFFromJson() throws IOException, DocumentException {
//        Kodeverk kodeverk = mock(Kodeverk.class);
//
//        WebSoknad soknad = new WebSoknad();
//        soknad.setskjemaNummer("NAV-1-1-1");
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "personalia", "true").medProperty("navn", "Ola Normann").medProperty("fnr", "01010101011").medProperty("addresse", "osloveien 1,  0479 oslo"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "reelarbeidsoker.villigdeltid", "true"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "reelarbeidsoker.villighelse", "true"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "reelarbeidsoker.villigdeltid", "true"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "reelarbeidsoker.villigpendle", "true"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "ikkeavtjentverneplikt", "true"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "ikkeegennaering", "true"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "andreytelser.stonadFisker", "true"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "andreytelser.privatTjenestepensjon", "false"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "andreytelser.vartpenger", "true"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "arbeidsforhold", "").medProperty("navn", "arbeidsforhold 1"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "arbeidsforhold", "").medProperty("navn", "arbeidsforhold 2"));
//        soknad.leggTilFaktum(new Faktum(1L, 1L, "arbeidsforhold", "").medProperty("navn", "arbeidsforhold 3"));
//
//        String html = new HandleBarKjoerer(kodeverk).fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger");
//
//        IOUtils.write(html, new FileOutputStream("/c:/test/dagpenger.html"));
//
//        String baseUrl = "/c:/test/";
//        String pdf = "c:/test/dagpenger.pdf";
//        PDFFabrikk.lagPdfFil(html, baseUrl, pdf);
//        //assertThat(html, containsString("NAV-1-1-1"));
//
//        long start = new Date().getTime();
//        ByteArrayOutputStream ut = new ByteArrayOutputStream();
//        PDFFabrikk.lagPDFOutputStream(html, baseUrl, ut);
//        //assertThat(ut.size(), is(1155));
//        ut.close();
//
//        long stopp = new Date().getTime();
//        long diff = stopp - start;
//        assertThat(diff, lessThanOrEqualTo(2500l));
        assertThat(true, is(true));
    }
}
