package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class VedleggOriginalFilerServiceTest {


    @Test
    public void lagerFilnavn() throws IOException {
        VedleggOriginalFilerService service = new VedleggOriginalFilerService();
        service.setUp();

        String filnavn = service.lagFilnavn("minfil.jpg", "image/jpeg", "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99");
        assertEquals("minfil-5c2a1cea.jpg", filnavn);

        String truncate = service.lagFilnavn("etkjempelangtfilnavn12345678901234567890123456789012345678901234567890.jpg",
                "image/jpeg", "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99");
        assertEquals("etkjempelangtfilnavn123456789012345678901234567890-5c2a1cea.jpg", truncate);

    }

}