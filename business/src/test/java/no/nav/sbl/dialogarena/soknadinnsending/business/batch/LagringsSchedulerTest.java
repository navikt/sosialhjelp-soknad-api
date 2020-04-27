package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LagringsSchedulerTest {

    @InjectMocks private LagringsScheduler scheduler = new LagringsScheduler();
    @Mock private HenvendelseService henvendelseService;
    @Mock private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Before
    public void setup() {
        System.setProperty("sendsoknad.batch.enabled", "true");
    }

    @Test
    public void skalAvbryteIHenvendelseOgSletteFraDatabase() throws InterruptedException {
        String behandlingsId = "2";
        String tilknyttetBehandlingsId = "1";
        long soknadId = 2;
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withSoknadId(soknadId)
                .withEier("11111111111")
                .withBehandlingsId(behandlingsId)
                .withInnsendingStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .withTilknyttetBehandlingsId(tilknyttetBehandlingsId);

        when(soknadUnderArbeidRepository.hentForeldedeEttersendelser()).thenReturn(Arrays.asList(soknadUnderArbeid));

        scheduler.slettForeldedeEttersendelserFraSoknadUnderArbeidDatabase();

        verify(henvendelseService).avbrytSoknad(behandlingsId, true);
        verify(soknadUnderArbeidRepository).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }

    @Test
    public void skalIkkeAvbryteIHenvendelseOgSletteFraDatabaseDersomDetIkkeErEttersendelse() throws InterruptedException {
        String behandlingsId = "2";
        long soknadId = 2;
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withSoknadId(soknadId)
                .withEier("11111111111")
                .withBehandlingsId(behandlingsId)
                .withInnsendingStatus(SoknadInnsendingStatus.UNDER_ARBEID) ;

        when(soknadUnderArbeidRepository.hentForeldedeEttersendelser()).thenReturn(Arrays.asList(soknadUnderArbeid));

        scheduler.slettForeldedeEttersendelserFraSoknadUnderArbeidDatabase();

        verify(henvendelseService, times(0)).avbrytSoknad(behandlingsId, true);
        verify(soknadUnderArbeidRepository, times(0)).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }

    @After
    public void teardown() {
        System.clearProperty("sendsoknad.batch.enabled");
    }

}
