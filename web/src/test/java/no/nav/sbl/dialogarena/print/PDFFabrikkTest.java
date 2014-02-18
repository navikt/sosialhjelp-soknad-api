package no.nav.sbl.dialogarena.print;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;

public class PDFFabrikkTest {

    @Test
    public void skalKunneLagePDF() {
        Kodeverk kodeverk = mock(Kodeverk.class);
        WebSoknad soknad = new WebSoknad();
        soknad.setSkjemaNummer("NAV-1-1-1");
        soknad.leggTilFaktum(new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("liste").medValue("testinnhold" ).medType(FaktumType.BRUKERREGISTRERT));
        soknad.leggTilFaktum(new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("liste").medValue("testinnhold2").medType(FaktumType.BRUKERREGISTRERT));
        soknad.leggTilFaktum(new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("liste").medValue("testinnhold3").medType(FaktumType.BRUKERREGISTRERT));
        soknad.leggTilFaktum(new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("liste").medValue("testinnhold4").medType(FaktumType.BRUKERREGISTRERT));
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
