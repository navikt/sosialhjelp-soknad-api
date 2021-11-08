package no.nav.sosialhjelp.soknad.business.batch;

import no.nav.sosialhjelp.soknad.business.batch.oppgave.Oppgave;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksData;
import no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks.FiksResultat;
import no.nav.sosialhjelp.soknad.business.db.config.DbTestConfig;
import no.nav.sosialhjelp.soknad.business.db.repositories.oppgave.OppgaveRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.BatchSendtSoknadRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.BatchSoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.client.leaderelection.LeaderElection;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import no.nav.sosialhjelp.soknad.domain.SoknadMetadataInnsendingStatus;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = {DbTestConfig.class})
class SlettLoggSchedulerTest {

    private static final String EIER = "11111111111";
    private static final int DAGER_GAMMEL_SOKNAD = 365;
    private static final String BEHANDLINGS_ID = "1100AAAAA";

    @Mock
    private LeaderElection leaderElection;
    @Mock
    private BatchSendtSoknadRepository batchSendtSoknadRepository;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private BatchSoknadMetadataRepository batchSoknadMetadataRepository;
    @Mock
    private OppgaveRepository oppgaveRepository;

    private SlettLoggScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SlettLoggScheduler(leaderElection, batchSoknadMetadataRepository, batchSendtSoknadRepository, oppgaveRepository, true);

        when(leaderElection.isLeader()).thenReturn(true);
    }

    @Test
    void skalSletteForeldetLoggFraDatabase() {
        Oppgave oppgave = oppgave(BEHANDLINGS_ID, DAGER_GAMMEL_SOKNAD + 1);
        SendtSoknad sendtSoknad = sendtSoknad(BEHANDLINGS_ID, EIER, DAGER_GAMMEL_SOKNAD + 1);
        SoknadMetadata soknadMetadata = soknadMetadata(BEHANDLINGS_ID, SoknadMetadataInnsendingStatus.UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1);
        when(batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD)).thenReturn(Optional.of(soknadMetadata)).thenReturn(Optional.empty());
        when(oppgaveRepository.hentOppgave(BEHANDLINGS_ID)).thenReturn(Optional.of(oppgave));
        when(batchSendtSoknadRepository.hentSendtSoknad(BEHANDLINGS_ID)).thenReturn(Optional.of(sendtSoknad.getSendtSoknadId()));

        scheduler.slettLogger();

        verify(oppgaveRepository).slettOppgave(BEHANDLINGS_ID);
        verify(batchSendtSoknadRepository).slettSendtSoknad(sendtSoknad.getSendtSoknadId());
        verify(batchSoknadMetadataRepository).slettSoknadMetaData(BEHANDLINGS_ID);
    }

    @Test
    void skalSletteForeldetLoggFraDatabaseSelvOmIkkeAlleTabelleneInneholderBehandlingsIdeen() {
        Oppgave oppgave = oppgave(BEHANDLINGS_ID, DAGER_GAMMEL_SOKNAD + 1);
        SendtSoknad sendtSoknad = sendtSoknad(BEHANDLINGS_ID, EIER, DAGER_GAMMEL_SOKNAD + 1);
        SoknadMetadata soknadMetadata = soknadMetadata(BEHANDLINGS_ID, SoknadMetadataInnsendingStatus.UNDER_ARBEID, DAGER_GAMMEL_SOKNAD + 1);
        when(batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD)).thenReturn(Optional.of(soknadMetadata)).thenReturn(Optional.empty());
        when(oppgaveRepository.hentOppgave(BEHANDLINGS_ID)).thenReturn(Optional.of(oppgave));
        when(batchSendtSoknadRepository.hentSendtSoknad(BEHANDLINGS_ID)).thenReturn(Optional.empty());

        scheduler.slettLogger();

        verify(oppgaveRepository).slettOppgave(BEHANDLINGS_ID);
        verify(batchSendtSoknadRepository, never()).slettSendtSoknad(sendtSoknad.getSendtSoknadId());
        verify(batchSoknadMetadataRepository).slettSoknadMetaData(BEHANDLINGS_ID);
    }

    @Test
    void skalIkkeSletteLoggSomErUnderEttAarGammelt() {
        when(batchSoknadMetadataRepository.hentEldreEnn(DAGER_GAMMEL_SOKNAD)).thenReturn(Optional.empty());

        scheduler.slettLogger();

        verify(oppgaveRepository, never()).slettOppgave(anyString());
        verify(batchSendtSoknadRepository, never()).slettSendtSoknad(anyLong());
        verify(batchSoknadMetadataRepository, never()).slettSoknadMetaData(anyString());
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

    private SendtSoknad sendtSoknad(String behandlingsId, String eier, int dagerSiden) {
        return new SendtSoknad()
                .withBehandlingsId(behandlingsId)
                .withNavEnhetsnavn("")
                .withOrgnummer("")
                .withBrukerOpprettetDato(LocalDateTime.now().minusDays(dagerSiden))
                .withBrukerFerdigDato(LocalDateTime.now().minusDays(dagerSiden))
                .withSendtDato(LocalDateTime.now().minusDays(dagerSiden))
                .withEier(eier)
                .withTilknyttetBehandlingsId("")
                .withFiksforsendelseId("")
                .withSendtSoknadId(1L);
    }

    private Oppgave oppgave(String behandlingsId, int dagerSiden) {
        Oppgave oppgave = new Oppgave();
        oppgave.behandlingsId = behandlingsId;
        oppgave.status = Oppgave.Status.FERDIG;
        oppgave.steg = 1;
        oppgave.id = 1L;
        oppgave.oppgaveData = new FiksData();
        oppgave.nesteForsok = null;
        oppgave.oppgaveResultat = new FiksResultat();
        oppgave.type = "";
        oppgave.opprettet = LocalDateTime.now().minusDays(dagerSiden);
        oppgave.sistKjort = LocalDateTime.now().minusDays(dagerSiden);

        return oppgave;
    }

}
