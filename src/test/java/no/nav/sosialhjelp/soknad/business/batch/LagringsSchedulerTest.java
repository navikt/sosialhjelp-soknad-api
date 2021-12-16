package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElection;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus;
import no.nav.sosialhjelp.soknad.innsending.HenvendelseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LagringsSchedulerTest {

    @Mock
    private LeaderElection leaderElection;
    @Mock
    private HenvendelseService henvendelseService;
    @Mock
    private BatchSoknadUnderArbeidRepository batchSoknadUnderArbeidRepository;

    private LagringsScheduler scheduler;

    @BeforeEach
    public void setup() {
        scheduler = new LagringsScheduler(leaderElection, henvendelseService, batchSoknadUnderArbeidRepository, true);

        when(leaderElection.isLeader()).thenReturn(true);
    }

    @Test
    void skalAvbryteIHenvendelseOgSletteFraDatabase() throws InterruptedException {
        String behandlingsId = "2";
        String tilknyttetBehandlingsId = "1";
        long soknadId = 2;
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withSoknadId(soknadId)
                .withEier("11111111111")
                .withBehandlingsId(behandlingsId)
                .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID)
                .withTilknyttetBehandlingsId(tilknyttetBehandlingsId);

        when(batchSoknadUnderArbeidRepository.hentForeldedeEttersendelser()).thenReturn(Arrays.asList(soknadUnderArbeid));

        scheduler.slettForeldedeEttersendelserFraSoknadUnderArbeidDatabase();

        verify(henvendelseService).avbrytSoknad(behandlingsId, true);
        verify(batchSoknadUnderArbeidRepository).slettSoknad(anyLong());
    }

    @Test
    void skalIkkeAvbryteIHenvendelseOgSletteFraDatabaseDersomDetIkkeErEttersendelse() throws InterruptedException {
        String behandlingsId = "2";
        long soknadId = 2;
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withSoknadId(soknadId)
                .withEier("11111111111")
                .withBehandlingsId(behandlingsId)
                .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID) ;

        when(batchSoknadUnderArbeidRepository.hentForeldedeEttersendelser()).thenReturn(Collections.singletonList(soknadUnderArbeid));

        scheduler.slettForeldedeEttersendelserFraSoknadUnderArbeidDatabase();

        verify(henvendelseService, times(0)).avbrytSoknad(behandlingsId, true);
        verify(batchSoknadUnderArbeidRepository, times(0)).slettSoknad(anyLong());
    }

}
