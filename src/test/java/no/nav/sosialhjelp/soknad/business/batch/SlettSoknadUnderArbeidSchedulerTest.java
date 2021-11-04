package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlettSoknadUnderArbeidSchedulerTest {

    @Mock
    private LeaderElection leaderElection;

    @Mock
    private BatchSoknadUnderArbeidRepository batchSoknadUnderArbeidRepository;

    private SlettSoknadUnderArbeidScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SlettSoknadUnderArbeidScheduler(leaderElection, batchSoknadUnderArbeidRepository, true);

        when(leaderElection.isLeader()).thenReturn(true);
    }

    @Test
    void skalSletteGamleSoknadUnderArbeid() {
        when(batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch())
                .thenReturn(Arrays.asList(1L, 2L));

        scheduler.slettGamleSoknadUnderArbeid();

         verify(batchSoknadUnderArbeidRepository, times(2)).slettSoknad(any());
    }
}