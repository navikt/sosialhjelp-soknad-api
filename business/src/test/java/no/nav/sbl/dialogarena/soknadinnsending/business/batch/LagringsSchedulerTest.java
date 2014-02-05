package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static no.nav.modig.lang.option.Optional.optional;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LagringsSchedulerTest {

    @InjectMocks private LagringsScheduler scheduler;
    @Mock private SoknadRepository soknadRepository;
    @Mock private FillagerConnector fillagerConnector;

    @Test
    public void leggerTilbakeSoknadenHvisNoeFeiler() throws InterruptedException {
        WebSoknad webSoknad = new WebSoknad();
        when(soknadRepository.plukkSoknadTilMellomlagring()).thenReturn(optional(webSoknad));
        doThrow(new RuntimeException("NEI!")).when(fillagerConnector).lagreFil(anyString(), anyString(), anyString(), any(InputStream.class));
        scheduler.lagreFilTilHenvendelseOgSlettILokalDb(optional(webSoknad));
        verify(soknadRepository).leggTilbake(webSoknad);
    }

}
