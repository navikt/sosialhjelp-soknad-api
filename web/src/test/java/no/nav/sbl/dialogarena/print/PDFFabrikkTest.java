package no.nav.sbl.dialogarena.print;

import static org.mockito.Mockito.mock;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class PDFFabrikkTest {

    @Test
    public void skalKunneLagePDF() {
        Kodeverk kodeverk = mock(Kodeverk.class);
        WebSoknad soknad = new WebSoknad();
        soknad.setSkjemaNummer("NAV-1-1-1");
        soknad.leggTilFaktum(new Faktum(1L, 1L, "liste", "testinnhold", FaktumType.BRUKERREGISTRERT));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "liste", "testinnhold2", FaktumType.BRUKERREGISTRERT));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "liste", "testinnhold3", FaktumType.BRUKERREGISTRERT));
        soknad.leggTilFaktum(new Faktum(1L, 1L, "liste", "testinnhold4", FaktumType.BRUKERREGISTRERT));
        String html;
        try {
            html = new HandleBarKjoerer(kodeverk).fyllHtmlMalMedInnhold(soknad,
                    "/html/WebSoknadHtml");
            byte[] pdfFil = PDFFabrikk.lagPdfFil(html);
            Assert.assertTrue(pdfFil.length > 0);
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }
}
