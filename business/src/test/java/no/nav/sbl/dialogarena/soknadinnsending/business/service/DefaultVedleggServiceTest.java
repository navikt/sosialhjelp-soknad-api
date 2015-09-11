package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.KVITTERING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg.Status.LastetOpp;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultVedleggServiceTest {

    private static final long SOKNAD_ID = 1L;
    private static final String BEHANDLING_ID = "1000000ABC";

    @Mock
    VedleggRepository vedleggRepository;

    @Mock
    SoknadRepository soknadRepository;

    @Mock
    FillagerService fillagerConnector;

    @InjectMocks
    VedleggService vedleggService = new VedleggService();

    @Test
    public void skalOppretteKvitteringHvisDenIkkeFinnes() {
        when(soknadRepository.hentSoknad(BEHANDLING_ID)).thenReturn(new WebSoknad().medBehandlingId("XXX").medAktorId("aktor-1"));
        byte[] kvittering = {'b', 'o', 'o', 'm'};
        vedleggService.lagreKvitteringSomVedlegg(BEHANDLING_ID, kvittering);
        verify(vedleggRepository).opprettVedlegg(any(Vedlegg.class), eq(kvittering));
    }

    @Test
    public void skalOppdatereKvitteringHvisDenAlleredeFinnes() {
        when(soknadRepository.hentSoknad(BEHANDLING_ID)).thenReturn(new WebSoknad().medBehandlingId(BEHANDLING_ID).medAktorId("aktor-1").medId(SOKNAD_ID));
        Vedlegg eksisterendeKvittering = new Vedlegg(SOKNAD_ID, null, KVITTERING, LastetOpp);
        when(vedleggRepository.hentVedleggForskjemaNummer(SOKNAD_ID, null, KVITTERING)).thenReturn(eksisterendeKvittering);
        byte[] kvitteringPdf = {'b', 'o', 'o', 'm'};
        vedleggService.lagreKvitteringSomVedlegg(BEHANDLING_ID, kvitteringPdf);
        verify(vedleggRepository).lagreVedleggMedData(SOKNAD_ID, eksisterendeKvittering.getVedleggId(), eksisterendeKvittering);
    }

}