package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.soknad.business.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SlettSoknadUnderArbeidSchedulerTest {

    private static final String EIER = "11111111111";
    private static final String BEHANDLINGS_ID = "1100AAAAA";

    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

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
        var soknadSkalIkkeSlettes = soknadUnderArbeid(1L, BEHANDLINGS_ID, SoknadUnderArbeidStatus.UNDER_ARBEID);
        var soknadSkalSlettes = soknadUnderArbeid(2L, BEHANDLINGS_ID, SoknadUnderArbeidStatus.UNDER_ARBEID);

        when(soknadUnderArbeidRepository.hentGamleSoknadUnderArbeidForBatch())
                .thenReturn(Arrays.asList(soknadSkalSlettes, soknadSkalIkkeSlettes));

        slettSoknadUnderArbeidScheduler.slettGamleSoknadUnderArbeid();

        // for senere
        // verify(soknadUnderArbeidRepository, times(2)).slettSoknad(any(), anyString());
    }

    private SoknadUnderArbeid soknadUnderArbeid(Long id, String behandlingsId, SoknadUnderArbeidStatus status) {
        return new SoknadUnderArbeid()
                .withSoknadId(id)
                .withBehandlingsId(behandlingsId)
                .withEier(EIER)
                .withStatus(status)
                .withJsonInternalSoknad(null)
                .withOpprettetDato(LocalDateTime.now().minusDays(12))
                .withSistEndretDato(LocalDateTime.now().minusDays(12))
                .withTilknyttetBehandlingsId(null)
                .withVersjon(1L);
    }
}