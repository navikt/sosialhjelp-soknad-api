package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.soknad.business.db.config.DbTestConfig;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.BatchSoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.BatchSoknadUnderArbeidRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElection;
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeidStatus;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {DbTestConfig.class})
class AvbrytAutomatiskSchedulerTest {

    private static final String EIER = "11111111111";
    private static final String BEHANDLINGS_ID = "1100AAAAA";
    private static final int DAGER_GAMMEL_SOKNAD = 14;

    @Mock
    private LeaderElection leaderElection;
    @Mock
    private BatchSoknadUnderArbeidRepository batchSoknadUnderArbeidRepository;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private BatchSoknadMetadataRepository batchSoknadMetadataRepository;

    private AvbrytAutomatiskSheduler scheduler;

    @BeforeEach
    public void setup() {
        scheduler = new AvbrytAutomatiskSheduler(leaderElection, soknadMetadataRepository, batchSoknadMetadataRepository, batchSoknadUnderArbeidRepository, true);

        when(leaderElection.isLeader()).thenReturn(true);
    }

    @Test
    void avbrytAutomatiskOgSlettGamleSoknader() {
        SoknadMetadata soknadMetadata = soknadMetadata(BEHANDLINGS_ID, SoknadMetadataInnsendingStatus.UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1);
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withSoknadId(1L)
                .withEier(EIER)
                .withBehandlingsId(BEHANDLINGS_ID)
                .withStatus(SoknadUnderArbeidStatus.UNDER_ARBEID);

        when(batchSoknadMetadataRepository.hentForBatch(DAGER_GAMMEL_SOKNAD)).thenReturn(Optional.of(soknadMetadata)).thenReturn(Optional.empty());
        when(batchSoknadUnderArbeidRepository.hentSoknadUnderArbeidIdFromBehandlingsIdOptional(BEHANDLINGS_ID)).thenReturn(Optional.of(soknadUnderArbeid.getSoknadId()));

        scheduler.avbrytGamleSoknader();

        ArgumentCaptor<SoknadMetadata> argument = ArgumentCaptor.forClass(SoknadMetadata.class);
        verify(soknadMetadataRepository).oppdater(argument.capture());
        SoknadMetadata oppdatertSoknadMetadata = argument.getValue();

        assertThat(oppdatertSoknadMetadata.status).isEqualTo(SoknadMetadataInnsendingStatus.AVBRUTT_AUTOMATISK);
        verify(batchSoknadUnderArbeidRepository).slettSoknad(anyLong());
    }

    private SoknadMetadata soknadMetadata(String behandlingsId, SoknadMetadataInnsendingStatus status, int dagerSiden) {
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
