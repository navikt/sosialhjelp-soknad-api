package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LagringsSchedulerTest {

    @InjectMocks private LagringsScheduler scheduler = new LagringsScheduler();
    @Mock private HenvendelseService henvendelseService;
    @Mock private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @Before
    public void setup() {
        System.setProperty("sendsoknad.batch.enabled", "true");
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid()));
    }

    @Ignore
    @Test
    public void skalFlytteAlleSoknaderTilHenvendelse() throws InterruptedException {
    }

    @Ignore
    @Test
    public void skalLagreSoknadIHenvendelseOgSletteFraDatabase() throws InterruptedException {
    }

    @Ignore
    @Test
    public void skalAvbryteIHenvendelseOgSletteFraDatabase() throws InterruptedException {
    }

    @Ignore
    @Test
    public void leggerTilbakeSoknadenHvisNoeFeiler() throws InterruptedException {
    }

    @After
    public void teardown() {
        System.clearProperty("sendsoknad.batch.enabled");
    }

}
