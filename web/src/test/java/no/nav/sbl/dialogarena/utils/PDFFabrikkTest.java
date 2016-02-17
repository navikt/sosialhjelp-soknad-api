package no.nav.sbl.dialogarena.utils;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.service.HandleBarKjoerer;
import no.nav.sbl.dialogarena.service.helpers.ForFaktaHelper;
import no.nav.sbl.dialogarena.service.helpers.faktum.ForFaktumHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.io.IOException;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.BRUKERREGISTRERT;

@RunWith(MockitoJUnitRunner.class)
public class PDFFabrikkTest {
    @InjectMocks
    private HandleBarKjoerer handleBarKjoerer;

    @Mock
    private MessageSource messageSource;

    @Mock
    private Kodeverk kodeverk;

    @Test
    public void skalKunneLagePDF() {
        WebSoknad soknad = new WebSoknad();
        soknad.setSkjemaNummer("NAV-1-1-1");
        soknad.leggTilFaktum(new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("liste").medValue("testinnhold" ).medType(BRUKERREGISTRERT));
        soknad.leggTilFaktum(new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("liste").medValue("testinnhold2").medType(BRUKERREGISTRERT));
        soknad.leggTilFaktum(new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("liste").medValue("testinnhold3").medType(BRUKERREGISTRERT));
        soknad.leggTilFaktum(new Faktum().medSoknadId(1L).medFaktumId(1L).medKey("liste").medValue("testinnhold4").medType(BRUKERREGISTRERT));
        String html;
        ForFaktumHelper forFaktumHelper = new ForFaktumHelper();
        ForFaktaHelper forFaktaHelper = new ForFaktaHelper();
        handleBarKjoerer.registrerHelper(forFaktumHelper.getNavn(), forFaktumHelper);
        handleBarKjoerer.registrerHelper(forFaktaHelper.getNavn(), forFaktaHelper);

        try {
            html = handleBarKjoerer.fyllHtmlMalMedInnhold(soknad, "/html/WebSoknadHtml");

            String skjemaPath = "file://" + PDFFabrikk.class.getResource("/").getPath();
            byte[] pdfFil = PDFFabrikk.lagPdfFil(html, skjemaPath);

            Assert.assertTrue(pdfFil.length > 0);
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }
}
