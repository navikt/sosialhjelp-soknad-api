package no.nav.sbl.dialogarena.soknadinnsending.business.batch;

import no.nav.sbl.dialogarena.sendsoknad.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.Oppgave;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksData;
import no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.FiksResultat;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.oppgave.OppgaveRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class SlettLoggSchedulerTest {

    private static final String EIER = "21036612271";
    private final int dagerGammelSoknad = 365;
    private final String behandlingsId = "1100AAAAA";

    @InjectMocks
    private SlettLoggScheduler scheduler = new SlettLoggScheduler();
    @Mock
    private SendtSoknadRepository sendtSoknadRepository;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private OppgaveRepository oppgaveRepository;

    @Before
    public void setup() {
        System.setProperty("sendsoknad.batch.enabled", "true");
    }

    @Test
    public void skalSletteForeldetLoggFraDatabase() {
        Oppgave oppgave = oppgave(behandlingsId, dagerGammelSoknad + 1);
        SendtSoknad sendtSoknad = sendtSoknad(behandlingsId, EIER, dagerGammelSoknad + 1);
        SoknadMetadata soknadMetadata = soknadMetadata(behandlingsId, SoknadInnsendingStatus.UNDER_ARBEID, dagerGammelSoknad + 1);
        when(soknadMetadataRepository.hentEldreEnn(dagerGammelSoknad)).thenReturn(Optional.of(soknadMetadata)).thenReturn(Optional.empty());
        when(oppgaveRepository.hentOppgave(behandlingsId)).thenReturn(Optional.of(oppgave));
        when(sendtSoknadRepository.hentSendtSoknad(behandlingsId, EIER)).thenReturn(Optional.of(sendtSoknad));

        scheduler.slettLogger();

        verify(oppgaveRepository).slettOppgave(behandlingsId);
        verify(sendtSoknadRepository).slettSendtSoknad(sendtSoknad, EIER);
        verify(soknadMetadataRepository).slettSoknadMetaData(behandlingsId, EIER);
    }

    @Test
    public void skalSletteForeldetLoggFraDatabaseSelvOmIkkeAlleTabelleneInneholderBehandlingsIdeen() {
        Oppgave oppgave = oppgave(behandlingsId, dagerGammelSoknad + 1);
        SendtSoknad sendtSoknad = sendtSoknad(behandlingsId, EIER, dagerGammelSoknad + 1);
        SoknadMetadata soknadMetadata = soknadMetadata(behandlingsId, SoknadInnsendingStatus.UNDER_ARBEID, dagerGammelSoknad + 1);
        when(soknadMetadataRepository.hentEldreEnn(dagerGammelSoknad)).thenReturn(Optional.of(soknadMetadata)).thenReturn(Optional.empty());
        when(oppgaveRepository.hentOppgave(behandlingsId)).thenReturn(Optional.of(oppgave));
        when(sendtSoknadRepository.hentSendtSoknad(behandlingsId, EIER)).thenReturn(Optional.empty());

        scheduler.slettLogger();

        verify(oppgaveRepository).slettOppgave(behandlingsId);
        verify(sendtSoknadRepository, never()).slettSendtSoknad(sendtSoknad, EIER);
        verify(soknadMetadataRepository).slettSoknadMetaData(behandlingsId, EIER);
    }

    @Test
    public void skalIkkeSletteLoggSomErUnderEttAarGammelt() {
        Oppgave oppgave = oppgave(behandlingsId, dagerGammelSoknad - 1);
        SendtSoknad sendtSoknad = sendtSoknad(behandlingsId, EIER, dagerGammelSoknad - 1);
        when(soknadMetadataRepository.hentEldreEnn(dagerGammelSoknad)).thenReturn(Optional.empty());
        when(oppgaveRepository.hentOppgave(behandlingsId)).thenReturn(Optional.of(oppgave));
        when(sendtSoknadRepository.hentSendtSoknad(behandlingsId, EIER)).thenReturn(Optional.of(sendtSoknad));

        scheduler.slettLogger();

        verify(oppgaveRepository, never()).slettOppgave(anyString());
        verify(sendtSoknadRepository, never()).slettSendtSoknad(any(SendtSoknad.class), anyString());
        verify(soknadMetadataRepository, never()).slettSoknadMetaData(anyString(), anyString());
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
        meta.hovedskjema = new SoknadMetadata.HovedskjemaMetadata();
        meta.hovedskjema.filUuid = null;

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
