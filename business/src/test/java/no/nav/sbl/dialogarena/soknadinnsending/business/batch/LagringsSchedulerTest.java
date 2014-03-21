package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static no.nav.modig.lang.option.Optional.none;
import static no.nav.modig.lang.option.Optional.optional;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LagringsSchedulerTest {

    @InjectMocks private LagringsScheduler scheduler = new LagringsScheduler();
    @Mock private SoknadRepository soknadRepository;
    @Mock private FillagerConnector fillagerConnector;


    @Test
    public void skalFlytteAlleSoknaderTilHenvendelse() throws InterruptedException {
        System.setProperty("sendsoknad.batch.enabled", "true");
        Optional<WebSoknad> tom = none();
        Optional<WebSoknad> soknad = Optional.optional(new WebSoknad().medId(1).medStatus(SoknadInnsendingStatus.UNDER_ARBEID));
        when(soknadRepository.plukkSoknadTilMellomlagring()).thenReturn(soknad, soknad, tom);
        scheduler.mellomlagreSoknaderOgNullstillLokalDb();
        verify(soknadRepository, times(2)).slettSoknad(anyLong());
    }

    @Test
    public void skalLagreSoknadIHenvendelseOgSletteFraDatabase() throws InterruptedException {
        WebSoknad webSoknad = new WebSoknad().medId(1).medAktorId("***REMOVED***").medBehandlingId("1").medUuid("1234").medStatus(SoknadInnsendingStatus.UNDER_ARBEID);
        when(soknadRepository.plukkSoknadTilMellomlagring()).thenReturn(optional(webSoknad));
        scheduler.lagreFilTilHenvendelseOgSlettILokalDb(optional(webSoknad));
        verify(fillagerConnector).lagreFil(eq(webSoknad.getBrukerBehandlingId()), eq(webSoknad.getUuid()), eq(webSoknad.getAktoerId()), any(InputStream.class));
        verify(soknadRepository).slettSoknad(webSoknad.getSoknadId());
    }

    @Test
    public void leggerTilbakeSoknadenHvisNoeFeiler() throws InterruptedException {
        WebSoknad webSoknad = new WebSoknad();
        when(soknadRepository.plukkSoknadTilMellomlagring()).thenReturn(optional(webSoknad));
        doThrow(new RuntimeException("NEI!")).when(fillagerConnector).lagreFil(anyString(), anyString(), anyString(), any(InputStream.class));
        scheduler.lagreFilTilHenvendelseOgSlettILokalDb(optional(webSoknad));
        verify(soknadRepository).leggTilbake(webSoknad);
    }

}
