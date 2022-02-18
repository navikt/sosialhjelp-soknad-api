package no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad;

import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
import no.nav.sosialhjelp.soknad.config.DbTestConfig;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DbTestConfig.class})
@ActiveProfiles("test")
class SendtSoknadRepositoryJdbcTest {

    private static final String EIER = "12345678901";
    private static final String EIER2 = "22222222222";
    private static final String BEHANDLINGSID = "1100020";
    private static final String TILKNYTTET_BEHANDLINGSID = "4567";
    private static final String FIKSFORSENDELSEID = "12345";
    private static final String ORGNUMMER = "987654";
    private static final String NAVENHETSNAVN = "NAV Enhet";
    private static final LocalDateTime BRUKER_OPPRETTET_DATO = now().minusDays(2).truncatedTo(ChronoUnit.MILLIS);
    private static final LocalDateTime BRUKER_FERDIG_DATO = now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS);
    private static final LocalDateTime SENDT_DATO = now().truncatedTo(ChronoUnit.MILLIS);

    @Inject
    private SendtSoknadRepository sendtSoknadRepository;

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @AfterEach
    public void tearDown() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SENDT_SOKNAD");
    }

    @Test
    void opprettSendtSoknadOppretterSendtSoknadIDatabasen() {
        Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER);

        assertThat(sendtSoknadId).isNotNull();
    }

    @Test
    void opprettSendtSoknadKasterRuntimeExceptionHvisEierErUlikSoknadseier() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER2));
    }

    @Test
    void opprettSendtSoknadKasterRuntimeExceptionHvisEierErNull() {
        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), null));
    }

    @Test
    void hentSendtSoknadHenterSendtSoknadForEierOgBehandlingsid() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER);

        SendtSoknad sendtSoknad = sendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID, EIER).get();

        assertThat(sendtSoknad.getEier()).isEqualTo(EIER);
        assertThat(sendtSoknad.getSendtSoknadId()).isNotNull();
        assertThat(sendtSoknad.getBehandlingsId()).isEqualTo(BEHANDLINGSID);
        assertThat(sendtSoknad.getTilknyttetBehandlingsId()).isEqualTo(TILKNYTTET_BEHANDLINGSID);
        assertThat(sendtSoknad.getFiksforsendelseId()).isEqualTo(FIKSFORSENDELSEID);
        assertThat(sendtSoknad.getOrgnummer()).isEqualTo(ORGNUMMER);
        assertThat(sendtSoknad.getNavEnhetsnavn()).isEqualTo(NAVENHETSNAVN);
        assertThat(sendtSoknad.getBrukerOpprettetDato()).isEqualTo(BRUKER_OPPRETTET_DATO);
        assertThat(sendtSoknad.getBrukerFerdigDato()).isEqualTo(BRUKER_FERDIG_DATO);
        assertThat(sendtSoknad.getSendtDato()).isEqualTo(SENDT_DATO);
    }

    @Test
    void oppdaterSendtSoknadVedSendingTilFiksOppdatererFiksIdOgSendtDato() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknadSomIkkeErSendtTilFiks(), EIER);

        sendtSoknadRepository.oppdaterSendtSoknadVedSendingTilFiks(FIKSFORSENDELSEID, BEHANDLINGSID, EIER);

        SendtSoknad oppdatertSendtSoknad = sendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID, EIER).get();
        assertThat(oppdatertSendtSoknad.getFiksforsendelseId()).isEqualTo(FIKSFORSENDELSEID);
        assertThat(oppdatertSendtSoknad.getSendtDato()).isNotNull();
    }

    private SendtSoknad lagSendtSoknad(String eier) {
        return lagSendtSoknad(eier, BEHANDLINGSID, FIKSFORSENDELSEID);
    }

    private SendtSoknad lagSendtSoknad(String eier, String behandlingsId, String fiksforsendelseId) {
        return new SendtSoknad().withEier(eier)
                .withBehandlingsId(behandlingsId)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withFiksforsendelseId(fiksforsendelseId)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withBrukerOpprettetDato(BRUKER_OPPRETTET_DATO)
                .withBrukerFerdigDato(BRUKER_FERDIG_DATO)
                .withSendtDato(SENDT_DATO);
    }

    private SendtSoknad lagSendtSoknadSomIkkeErSendtTilFiks() {
        return new SendtSoknad().withEier(EIER)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withBrukerOpprettetDato(BRUKER_OPPRETTET_DATO)
                .withBrukerFerdigDato(BRUKER_FERDIG_DATO);
    }
}