package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.BatchSoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SlettSoknadUnderArbeidSchedulerTest {

    @Mock
    private BatchSoknadUnderArbeidRepository batchSoknadUnderArbeidRepository;

    @InjectMocks
    private SlettSoknadUnderArbeidScheduler slettSoknadUnderArbeidScheduler;

    @Before
    public void setup() {
        System.setProperty("sendsoknad.batch.enabled", "true");
    }

    @After
    public void teardown() {
        System.clearProperty("sendsoknad.batch.enabled");
    }

    @Test
    public void skalSletteGamleSoknadUnderArbeid() {
        when(batchSoknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch())
                .thenReturn(Arrays.asList(1L, 2L));

        slettSoknadUnderArbeidScheduler.slettGamleSoknadUnderArbeid();

         verify(batchSoknadUnderArbeidRepository, times(2)).slettSoknad(any());
    }
}