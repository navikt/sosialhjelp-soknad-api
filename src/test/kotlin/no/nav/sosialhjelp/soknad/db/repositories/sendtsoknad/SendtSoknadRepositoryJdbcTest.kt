package no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad

import no.nav.sosialhjelp.soknad.Application
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@ActiveProfiles(profiles = ["no-redis", "test"])
@SpringBootTest(classes = [Application::class])
internal class SendtSoknadRepositoryJdbcTest {

    @Inject
    private lateinit var sendtSoknadRepository: SendtSoknadRepository

    @Inject
    private lateinit var jdbcTemplate: JdbcTemplate

    @AfterEach
    fun tearDown() {
        jdbcTemplate.update("delete from SENDT_SOKNAD")
    }

    @Test
    fun opprettSendtSoknadOppretterSendtSoknadIDatabasen() {
        val sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER)
        assertThat(sendtSoknadId).isNotNull
    }

    @Test
    fun opprettSendtSoknadKasterRuntimeExceptionHvisEierErUlikSoknadseier() {
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER2) }
    }

    @Test
    fun opprettSendtSoknadKasterRuntimeExceptionHvisEierErNull() {
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), null) }
    }

    @Test
    fun hentSendtSoknadHenterSendtSoknadForEierOgBehandlingsid() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER)
        val sendtSoknad = sendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID, EIER).get()
        assertThat(sendtSoknad.eier).isEqualTo(EIER)
        assertThat(sendtSoknad.sendtSoknadId).isNotNull
        assertThat(sendtSoknad.behandlingsId).isEqualTo(BEHANDLINGSID)
        assertThat(sendtSoknad.tilknyttetBehandlingsId).isEqualTo(TILKNYTTET_BEHANDLINGSID)
        assertThat(sendtSoknad.fiksforsendelseId).isEqualTo(FIKSFORSENDELSEID)
        assertThat(sendtSoknad.orgnummer).isEqualTo(ORGNUMMER)
        assertThat(sendtSoknad.navEnhetsnavn).isEqualTo(NAVENHETSNAVN)
        assertThat(sendtSoknad.brukerOpprettetDato).isEqualTo(BRUKER_OPPRETTET_DATO)
        assertThat(sendtSoknad.brukerFerdigDato).isEqualTo(BRUKER_FERDIG_DATO)
        assertThat(sendtSoknad.sendtDato).isEqualTo(SENDT_DATO)
    }

    @Test
    fun oppdaterSendtSoknadVedSendingTilFiksOppdatererFiksIdOgSendtDato() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknadSomIkkeErSendtTilFiks(), EIER)
        sendtSoknadRepository.oppdaterSendtSoknadVedSendingTilFiks(FIKSFORSENDELSEID, BEHANDLINGSID, EIER)
        val oppdatertSendtSoknad = sendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID, EIER).get()
        assertThat(oppdatertSendtSoknad.fiksforsendelseId).isEqualTo(FIKSFORSENDELSEID)
        assertThat(oppdatertSendtSoknad.sendtDato).isNotNull
    }

    private fun lagSendtSoknad(
        eier: String,
        behandlingsId: String = BEHANDLINGSID,
        fiksforsendelseId: String = FIKSFORSENDELSEID,
    ): SendtSoknad {
        return SendtSoknad(
            behandlingsId = behandlingsId,
            tilknyttetBehandlingsId = TILKNYTTET_BEHANDLINGSID,
            eier = eier,
            fiksforsendelseId = fiksforsendelseId,
            orgnummer = ORGNUMMER,
            navEnhetsnavn = NAVENHETSNAVN,
            brukerOpprettetDato = BRUKER_OPPRETTET_DATO,
            brukerFerdigDato = BRUKER_FERDIG_DATO,
            sendtDato = SENDT_DATO
        )
    }

    private fun lagSendtSoknadSomIkkeErSendtTilFiks(): SendtSoknad {
        return SendtSoknad(
            behandlingsId = BEHANDLINGSID,
            tilknyttetBehandlingsId = TILKNYTTET_BEHANDLINGSID,
            eier = EIER,
            fiksforsendelseId = null,
            orgnummer = ORGNUMMER,
            navEnhetsnavn = NAVENHETSNAVN,
            brukerOpprettetDato = BRUKER_OPPRETTET_DATO,
            brukerFerdigDato = BRUKER_FERDIG_DATO,
            sendtDato = null
        )
    }

    companion object {
        private const val EIER = "12345678901"
        private const val EIER2 = "22222222222"
        private const val BEHANDLINGSID = "1100020"
        private const val TILKNYTTET_BEHANDLINGSID = "4567"
        private const val FIKSFORSENDELSEID = "12345"
        private const val ORGNUMMER = "987654"
        private const val NAVENHETSNAVN = "NAV Enhet"
        private val BRUKER_OPPRETTET_DATO = LocalDateTime.now().minusDays(2).truncatedTo(ChronoUnit.MILLIS)
        private val BRUKER_FERDIG_DATO = LocalDateTime.now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS)
        private val SENDT_DATO = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    }
}
