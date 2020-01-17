package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class AvbrytAutomatiskSchedulerTest {

    private static final String EIER = "11111111111";
    private static final String BEHANDLINGS_ID = "1100AAAAA";
    private static final int DAGER_GAMMEL_SOKNAD = 14;

    @InjectMocks
    private AvbrytAutomatiskSheduler scheduler = new AvbrytAutomatiskSheduler();
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @Before
    public void setup() {
        System.setProperty("sendsoknad.batch.enabled", "true");
    }

    @Test
    public void avbrytAutomatiskOgSlettGamleSoknader() {
        SoknadMetadata soknadMetadata = soknadMetadata(BEHANDLINGS_ID, SoknadInnsendingStatus.UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withSoknadId(1L)
                .withEier(EIER)
                .withBehandlingsId(BEHANDLINGS_ID)
                .withInnsendingStatus(SoknadInnsendingStatus.UNDER_ARBEID);

        when(soknadMetadataRepository.hentForBatch(DAGER_GAMMEL_SOKNAD)).thenReturn(Optional.of(soknadMetadata)).thenReturn(Optional.empty());
        when(soknadUnderArbeidRepository.hentSoknadOptional(BEHANDLINGS_ID, EIER)).thenReturn(Optional.of(soknadUnderArbeid));

        scheduler.avbrytGamleSoknader();

        ArgumentCaptor<SoknadMetadata> argument = ArgumentCaptor.forClass(SoknadMetadata.class);
        verify(soknadMetadataRepository).oppdater(argument.capture());
        SoknadMetadata oppdatertSoknadMetadata = argument.getValue();

        assertThat(oppdatertSoknadMetadata.status, is(SoknadInnsendingStatus.AVBRUTT_AUTOMATISK));
        verify(soknadUnderArbeidRepository).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }

    @After
    public void teardown() {
        System.clearProperty("sendsoknad.batch.enabled");
    }

    private SoknadMetadata soknadMetadata(String behandlingsId, SoknadInnsendingStatus status, int dagerSiden) {
        SoknadMetadata meta = new SoknadMetadata();
        meta.id = soknadMetadataRepository.hentNesteId();
        meta.behandlingsId = behandlingsId;
        meta.fnr = EIER;
        meta.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
        meta.skjema = "";
        meta.status = status;
        meta.innsendtDato = LocalDateTime.now().minusDays(dagerSiden);
        meta.opprettetDato = LocalDateTime.now().minusDays(dagerSiden);
        meta.sistEndretDato = LocalDateTime.now().minusDays(dagerSiden);

        return meta;
    }
}
