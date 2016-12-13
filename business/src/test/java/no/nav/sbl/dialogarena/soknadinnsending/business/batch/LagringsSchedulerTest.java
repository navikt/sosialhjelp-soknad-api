package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

@RunWith(MockitoJUnitRunner.class)
public class LagringsSchedulerTest {

    @InjectMocks private LagringsScheduler scheduler = new LagringsScheduler();
    @Mock private SoknadRepository soknadRepository;
    @Mock private FillagerService fillagerService;
    @Mock private HenvendelseService henvendelseService;


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
        WebSoknad webSoknad = new WebSoknad().medId(1).medAktorId("11111111111").medBehandlingId("1").medUuid("1234").medStatus(SoknadInnsendingStatus.UNDER_ARBEID);
        when(soknadRepository.plukkSoknadTilMellomlagring()).thenReturn(optional(webSoknad));
        scheduler.lagreFilTilHenvendelseOgSlettILokalDb(optional(webSoknad));
        verify(fillagerService).lagreFil(eq(webSoknad.getBrukerBehandlingId()), eq(webSoknad.getUuid()), eq(webSoknad.getAktoerId()), any(InputStream.class));
        verify(soknadRepository).slettSoknad(webSoknad.getSoknadId());
    }

    @Test
    public void skalAvbryteIHenvendelseOgSletteFraDatabase() throws InterruptedException {
        String behandlingsId = "1";
        int soknadId = 1;
        WebSoknad webSoknad = new WebSoknad()
                .medId(soknadId)
                .medAktorId("11111111111")
                .medBehandlingId(behandlingsId)
                .medUuid("1234")
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medStatus(SoknadInnsendingStatus.UNDER_ARBEID);
        when(soknadRepository.plukkSoknadTilMellomlagring()).thenReturn(optional(webSoknad),Optional.<WebSoknad>none());
        scheduler.mellomlagreSoknaderOgNullstillLokalDb();
        verify(henvendelseService).avbrytSoknad(behandlingsId);
        verify(soknadRepository).slettSoknad(webSoknad.getSoknadId());
    }

    @Test
    public void leggerTilbakeSoknadenHvisNoeFeiler() throws InterruptedException {
        WebSoknad webSoknad = new WebSoknad();
        when(soknadRepository.plukkSoknadTilMellomlagring())
                .thenReturn(optional(webSoknad))
                .thenReturn(Optional.<WebSoknad>none());
        doThrow(new RuntimeException("NEI!")).when(fillagerService).lagreFil(anyString(), anyString(), anyString(), any(InputStream.class));
        scheduler.mellomlagreSoknaderOgNullstillLokalDb();
        verify(soknadRepository).leggTilbake(webSoknad);
    }

}
