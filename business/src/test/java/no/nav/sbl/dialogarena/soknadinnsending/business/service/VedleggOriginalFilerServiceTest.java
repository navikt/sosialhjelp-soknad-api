package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg.Status.VedleggKreves;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VedleggOriginalFilerServiceTest {

    @Mock
    VedleggRepository vedleggRepository;

    @Mock
    SoknadRepository soknadRepository;

    @Mock
    FillagerService fillagerService;

    @InjectMocks
    VedleggOriginalFilerService vedleggOriginalFilerService;

    @Test
    public void lagerFilnavn() throws IOException {
        VedleggOriginalFilerService service = new VedleggOriginalFilerService();
        service.setUp();

        String filnavn = service.lagFilnavn("minfil.jpg", "image/jpeg", "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99");
        assertEquals("minfil-5c2a1cea.jpg", filnavn);

        String truncate = service.lagFilnavn("etkjempelangtfilnav***REMOVED***78901234567890123456789012345678901234567890.jpg",
                "image/jpeg", "5c2a1cea-ef05-4db6-9c98-1b6c9b3faa99");
        assertEquals("etkjempelangtfilnav***REMOVED***789012345678901234567890-5c2a1cea.jpg", truncate);

    }

    @Test
    public void sletterFaktaOgVedleggOmFlere() {
        when(vedleggRepository.hentVedlegg(1234L)).thenReturn(
                new Vedlegg()
                        .medSoknadId(9999L)
                        .medFaktumId(4567L)
                        .medFillagerReferanse("filuid-1234")
        );

        when(soknadRepository.hentSoknad(9999L)).thenReturn(
                new WebSoknad()
                        .medId(9999L)
                        .medFaktum(new Faktum().medKey("opplysning.vedlegg").medFaktumId(4567L))
                        .medFaktum(new Faktum().medKey("opplysning.vedlegg"))
                        .medFaktum(new Faktum().medKey("opplysning.noeabsoluttheltannet"))
        );

        vedleggOriginalFilerService.slettOriginalVedlegg(1234L);

        verify(vedleggRepository).slettVedleggMedVedleggId(1234L);
        verify(soknadRepository).slettBrukerFaktum(9999L, 4567L);
        verify(fillagerService).slettFil("filuid-1234");
    }

    @Test
    public void resetterBareVedleggOmSisteSlettes() {
        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(1234L)
                .medSoknadId(9999L)
                .medFaktumId(4567L)
                .medFillagerReferanse("filuid-1234");
        when(vedleggRepository.hentVedlegg(1234L)).thenReturn(vedlegg);

        when(soknadRepository.hentSoknad(9999L)).thenReturn(
                new WebSoknad()
                        .medId(9999L)
                        .medFaktum(new Faktum().medKey("opplysning.vedlegg").medFaktumId(4567L))
        );

        vedleggOriginalFilerService.slettOriginalVedlegg(1234L);

        verify(vedleggRepository).lagreVedleggMedData(9999L, 1234L, vedlegg);
        assertEquals(VedleggKreves, vedlegg.getInnsendingsvalg());
        verify(vedleggRepository, times(0)).slettVedleggMedVedleggId(anyLong());
    }

}